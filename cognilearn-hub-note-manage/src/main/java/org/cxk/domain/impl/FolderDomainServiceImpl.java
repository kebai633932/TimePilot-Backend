package org.cxk.domain.impl;

import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import lombok.extern.slf4j.Slf4j;
import org.cxk.domain.model.entity.FolderEntity;
import org.cxk.domain.repository.INoteRepository;
import org.cxk.infrastructure.adapter.dao.po.Folder;
import org.cxk.domain.IFolderDomainService;
import org.cxk.domain.repository.IFolderRepository;
import org.cxk.util.RedisKeyPrefix;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import types.exception.BizException;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 文件夹业务流程实现类
 */
@Slf4j
@Service
public class FolderDomainServiceImpl implements IFolderDomainService {

    @Resource
    private IFolderRepository folderRepository;
    @Resource
    private INoteRepository noteRepository;
    /** 最大文件夹树高 */
    private static final int MAX_TREE_DEPTH = 5;

    @Override
    public Long createFolder(Long userId, String name, Long parentId) {
        // 1. 校验层级限制
        validateTreeDepth(userId, parentId);

        // 2. 检查同名文件夹（唯一性约束）
        if (folderRepository.existsByUserIdAndParentIdAndName(userId, parentId, name)) {
            throw new BizException("该文件夹已存在");
        }

        // 3. 生成新文件夹 ID 并保存
        Long folderId = TinyId.nextId("folder_create");
        FolderEntity folderEntity=new FolderEntity();
        folderEntity.setFolderId(folderId);
        folderEntity.setUserId(userId);
        folderEntity.setName(name);
        folderEntity.setParentId(parentId);

        folderRepository.save(folderEntity);
        return folderId;
    }


    @Override
    public void updateFolder(Long userId, String name, Long folderId, Long newParentId) {
        // 1. 获取文件夹 & 权限校验
        FolderEntity folderEntity = folderRepository.findByFolderIdAndUserId(folderId, userId)
                .orElseThrow(() -> new BizException("文件夹不存在或权限不足"));

        boolean parentChanged = !Objects.equals(folderEntity.getParentId(), newParentId);
        boolean nameChanged = name != null && !name.trim().isEmpty() &&
                !name.trim().equals(folderEntity.getName());

        // 2. 如果没有任何修改，直接返回
        if (!parentChanged && !nameChanged) {
            return ;
        }

        // 3. 如果父节点有变化
        if (parentChanged) {
            validateBidirectionalTreeDepth(userId,folderId, newParentId);
            preventCircularDependency(folderId, newParentId);
            folderEntity.setParentId(newParentId);
        }

        // 4. 如果名称有变化
        if (nameChanged) {
            if (folderRepository.existsByUserIdAndParentIdAndName(userId, folderEntity.getParentId(), name.trim())) {
                throw new BizException("该名称的文件夹已存在");
            }
            folderEntity.setName(name.trim());
        }

        // 5. 保存变更
        folderRepository.save(folderEntity);
    }

    @Override
    public void deleteFolder(Long userId, Long folderId) {
        // 1. 获取文件夹 & 权限校验
        FolderEntity folderEntity = folderRepository.findByFolderIdAndUserId(folderId, userId)
                .orElseThrow(() -> new BizException("文件夹不存在或权限不足"));

        // 2. 检查是否为空文件夹
        if (folderRepository.countByParentId(folderId) > 0 && noteRepository.countByParentId(folderId) > 0 ) {
            throw new BizException("文件夹非空，无法删除");
        }

        // 3. 删除记录
        folderRepository.delete(folderEntity);
    }

    /**
     * 验证文件夹树高（防止超过 MAX_TREE_DEPTH），用于创建
     */
    private void validateTreeDepth(Long userId, Long parentId) {
        if (parentId == null || parentId == 0) return;

        int depth = 1;
        Long currentFolderId = parentId;

        while (currentFolderId != null && currentFolderId != 0) {
            depth++;
            if (depth > MAX_TREE_DEPTH) {
                throw new BizException("文件夹层级不能超过 " + MAX_TREE_DEPTH + " 层");
            }

            FolderEntity parent = folderRepository.findByFolderId(currentFolderId)
                    .orElseThrow(() -> new BizException("父文件夹不存在"));

            if (!userId.equals(parent.getUserId())) {
                throw new BizException("父文件夹不属于当前用户");
            }

            currentFolderId = parent.getParentId();
        }
    }


    /**
     * 验证文件夹树高（防止超过 MAX_TREE_DEPTH），用于修改
     * 文件夹树高是上下一起算
     */
    private void validateBidirectionalTreeDepth(Long userId, Long folderId, Long newParentId) {
        if (newParentId == null || newParentId == 0) return;

        // 1. 向上计算父链高度
        int upDepth = 1; // 从 newParentId 开始
        Long currentFolderId = newParentId;
        while (currentFolderId != null && currentFolderId != 0) {
            upDepth++;
            if (upDepth > MAX_TREE_DEPTH) {
                throw new BizException("移动后文件夹层级不能超过 " + MAX_TREE_DEPTH + " 层");
            }

            FolderEntity parent = folderRepository.findByFolderId(currentFolderId)
                    .orElseThrow(() -> new BizException("父文件夹不存在"));

            if (!userId.equals(parent.getUserId())) {
                throw new BizException("父文件夹不属于当前用户");
            }
            currentFolderId = parent.getParentId();
        }

        // 2. 向下计算当前 folder 的最大子树深度
        int downDepth = getSubtreeMaxDepth(folderId);

        // 3. 合并高度（减 1 避免重复计算当前 folder）
        int totalDepth = upDepth + downDepth - 1;
        if (totalDepth > MAX_TREE_DEPTH) {
            throw new BizException("移动后文件夹层级不能超过 " + MAX_TREE_DEPTH + " 层");
        }
    }
    /**
     * 获取某个文件夹的最大子树深度（bfs）
     */
    private int getSubtreeMaxDepth(Long rootId) {
        if(rootId==null){
            throw new BizException("无该文件夹id");
        }
        List<Long> currentLevel = Collections.singletonList(rootId);
        int depth = 0;

        while (!currentLevel.isEmpty()) {
            depth++; // 当前层计数
            currentLevel = folderRepository.findByParentIdList(currentLevel); // 获取下一层节点
            // 深度限制，提前退出
            if (depth > MAX_TREE_DEPTH) {
                throw new BizException("文件夹层级不能超过 " + MAX_TREE_DEPTH + " 层");
            }
        }

        return depth;
    }

    /**
     * 防止移动文件夹形成循环依赖
     */
    private void preventCircularDependency(Long folderId, Long newParentId) {
        if (folderId.equals(newParentId)) {
            throw new BizException("不能将文件夹移动到自身");
        }

        Long currentId = newParentId;
        int depth = 1;
        while (currentId != null && currentId != 0) {
            if (currentId.equals(folderId)) {
                throw new BizException("不能将文件夹移动到自己的子文件夹");
            }
            // 深度限制，提前退出
            if (depth > MAX_TREE_DEPTH) {
                throw new BizException("文件夹层级不能超过 " + MAX_TREE_DEPTH + " 层");
            }
            FolderEntity parent = folderRepository.findByFolderId(currentId)
                    .orElseThrow(() -> new BizException("父文件夹不存在"));

            currentId = parent.getParentId();
            depth++;
        }
    }
}
