package org.cxk.service.impl;

import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import lombok.extern.slf4j.Slf4j;
import org.cxk.infrastructure.adapter.dao.po.Folder;
import org.cxk.service.IFolderService;
import org.cxk.service.repository.IFolderRepository;
import org.cxk.util.RedisKeyPrefix;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import types.exception.BizException;
import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 文件夹业务实现类
 * 核心策略：
 *  - 对同一用户的目录结构操作，加“用户级分布式锁”保证互斥
 *  - 在锁的保护下执行数据库事务，保证数据一致性
 *  - 事务内完成数据库操作 + 缓存刷新（删除缓存）
 *  - 事务提交后统一释放锁
 */
@Slf4j
@Service
public class FolderServiceImpl implements IFolderService {

    @Resource
    private IFolderRepository folderRepository;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private TransactionTemplate transactionTemplate;

    /** 获取锁最大等待秒数（防止无限等待） */
    private static final int LOCK_WAIT = 3;
    /** 锁自动释放时间（秒），避免宕机导致死锁 */
    private static final int LOCK_LEASE = 10;
    /** 最大文件夹树高 */
    private static final int MAX_TREE_DEPTH = 3;

    @Override
    public Long createFolder(Long userId, String name, Long parentId) {
        String lockKey = buildUserFolderLockKey(userId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 获取分布式锁（用户级别），防止并发修改同一用户的文件夹树
            if (!lock.tryLock(LOCK_WAIT, LOCK_LEASE, TimeUnit.SECONDS)) {
                throw new BizException("系统繁忙，请稍后再试");
            }

            // 在锁内执行数据库事务
            return transactionTemplate.execute(status -> {
                // 1. 校验层级限制
                validateTreeDepth(userId, parentId);

                // 2. 检查同名文件夹（唯一性约束）
                if (folderRepository.existsByUserIdAndParentIdAndName(userId, parentId, name)) {
                    throw new BizException("该文件夹已存在");
                }

                // 3. 生成新文件夹 ID 并保存
                Long folderId = TinyId.nextId("folder_create");
                Folder folder = new Folder();
                folder.setFolderId(folderId);
                folder.setUserId(userId);
                folder.setName(name);
                folder.setParentId(parentId);
                folderRepository.save(folder);

                // 4. 注册事务提交后的回调，清理缓存
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                try {
                                    clearUserFolderListCache(userId);
                                } catch (Exception e) {
                                    log.error("清理用户文件夹缓存异常", e);
                                }
                            }
                        }
                );
                return folderId;
            });

        } catch (InterruptedException e) {
            // 线程中断处理
            Thread.currentThread().interrupt();
            throw new BizException("系统异常，请稍后重试");
        } finally {
            // 保证最终释放锁（即使事务异常）
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("释放锁异常", e);
            }
        }
    }

    @Override
    public void updateFolder(Long userId, String name, Long folderId, Long newParentId) {
        String lockKey = buildUserFolderLockKey(userId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(LOCK_WAIT, LOCK_LEASE, TimeUnit.SECONDS)) {
                throw new BizException("系统繁忙，请稍后再试");
            }

            transactionTemplate.execute(status -> {
                // 1. 获取文件夹 & 权限校验
                Folder folder = (Folder) folderRepository.findByFolderIdAndUserId(folderId, userId)
                        .orElseThrow(() -> new BizException("文件夹不存在或权限不足"));

                boolean parentChanged = !Objects.equals(folder.getParentId(), newParentId);
                boolean nameChanged = name != null && !name.trim().isEmpty() &&
                        !name.trim().equals(folder.getName());

                // 2. 如果没有任何修改，直接返回
                if (!parentChanged && !nameChanged) {
                    return null;
                }

                // 3. 如果父节点有变化
                if (parentChanged) {
                    validateTreeDepth(userId, newParentId);
                    preventCircularDependency(folderId, newParentId);
                    folder.setParentId(newParentId);
                }

                // 4. 如果名称有变化
                if (nameChanged) {
                    if (folderRepository.existsByUserIdAndParentIdAndName(userId, folder.getParentId(), name.trim())) {
                        throw new BizException("该名称的文件夹已存在");
                    }
                    folder.setName(name.trim());
                }

                // 5. 保存变更
                folderRepository.save(folder);

                // 6. 注册事务提交后的回调，清理缓存
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                try {
                                    clearFolderInfoCache(folderId);
                                    clearUserFolderListCache(userId);
                                } catch (Exception e) {
                                    log.error("清理用户文件夹缓存异常", e);
                                }
                            }
                        }
                );

                return null;
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("系统异常，请稍后重试");
        } finally {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("释放锁异常", e);
            }
        }
    }


    @Override
    public void deleteFolder(Long userId, Long folderId) {
        String lockKey = buildUserFolderLockKey(userId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(LOCK_WAIT, LOCK_LEASE, TimeUnit.SECONDS)) {
                throw new BizException("系统繁忙，请稍后再试");
            }

            transactionTemplate.execute(status -> {
                // 1. 获取文件夹 & 权限校验
                Folder folder = (Folder) folderRepository.findByFolderIdAndUserId(folderId, userId)
                        .orElseThrow(() -> new BizException("文件夹不存在或权限不足"));

                // 2. 检查是否为空文件夹
                if (folderRepository.countByParentId(folderId) > 0) {
                    throw new BizException("文件夹非空，无法删除");
                }

                // 3. 删除记录
                folderRepository.delete(folder);

                // 4. 删除缓存
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                try {
                                    clearFolderInfoCache(folderId);
                                    clearUserFolderListCache(userId);
                                } catch (Exception e) {
                                    log.error("清理用户文件夹缓存异常", e);
                                }
                            }
                        }
                );
                return null;
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("系统异常，请稍后重试");
        } finally {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("释放锁异常", e);
            }
        }
    }

    /**
     * 验证文件夹树高（防止超过 MAX_TREE_DEPTH）
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

            Folder parent = folderRepository.findByFolderId(currentFolderId)
                    .orElseThrow(() -> new BizException("父文件夹不存在"));

            if (!userId.equals(parent.getUserId())) {
                throw new BizException("父文件夹不属于当前用户");
            }

            currentFolderId = parent.getParentId();
        }
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
            Folder parent = folderRepository.findByFolderId(currentId)
                    .orElseThrow(() -> new BizException("父文件夹不存在"));

            currentId = parent.getParentId();
            depth++;
        }
    }

    /** 删除用户的文件夹列表缓存（存在才删除） */
    private void clearUserFolderListCache(Long userId) {
        String cacheKey = RedisKeyPrefix.USER_FOLDER_LIST.format(userId);
        redissonClient.getKeys().delete(cacheKey); // key 不存在也安全
    }

    /** 删除单个文件夹信息缓存（存在才删除） */
    private void clearFolderInfoCache(Long folderId) {
        String cacheKey = RedisKeyPrefix.FOLDER_INFO.format(folderId);
        redissonClient.getKeys().delete(cacheKey); // key 不存在也安全
    }

    /** 构造用户目录锁的 Key */
    private String buildUserFolderLockKey(Long userId) {
        return "lock:user:" + userId + ":folderTree";
    }
}
