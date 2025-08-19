package org.cxk.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.cxk.domain.model.entity.FolderEntity;
import org.cxk.infrastructure.adapter.dao.IFolderDao;
import org.cxk.infrastructure.adapter.dao.po.Folder;
import org.cxk.domain.repository.IFolderRepository;
import org.cxk.util.RedisKeyPrefix;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author KJH
 * @description 文件夹
 * @create 2025/8/14 20:41
 */
@Repository
@AllArgsConstructor
public class FolderRepository implements IFolderRepository {
    private final IFolderDao folderDao;
    private final RedissonClient redissonClient;
    @Override
    public boolean existsByUserIdAndParentIdAndName(Long userId, Long parentId, String name) {
        LambdaQueryWrapper<Folder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Folder::getUserId, userId)
                .eq(Folder::getParentId, parentId)
                .eq(Folder::getName, name)
                .last("LIMIT 1"); // 提高查询效率
        return folderDao.selectCount(wrapper) > 0;
    }

    @Override
    public Optional<FolderEntity> findByFolderId(Long folderId) {
        Folder folder = folderDao.findByFolderId(folderId);
        return Optional.ofNullable(folder)
                .map(f -> FolderEntity.builder()
                        .folderId(f.getFolderId())
                        .userId(f.getUserId())
                        .parentId(f.getParentId())
                        .name(f.getName())
                        .deleteTime(f.getDeleteTime())
                        .build());
    }

    @Override
    public void save(FolderEntity folderEntity) {
        Folder folder = new Folder();
        folder.setFolderId(folderEntity.getFolderId());
        folder.setUserId(folderEntity.getUserId());
        folder.setName(folderEntity.getName());
        folder.setParentId(folderEntity.getParentId());
        if (folder.getId() == null) {
            folderDao.insert(folder);
        } else {
            folderDao.updateById(folder);
        }
    }

    @Override
    public int countByParentId(Long folderId) {
        LambdaQueryWrapper<Folder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Folder::getParentId, folderId);
        return folderDao.selectCount(wrapper).intValue();
    }

    @Override
    public void delete(FolderEntity folderEntity) {
        Folder folder=new Folder();
        folder.setFolderId(folderEntity.getFolderId());
        folder.setUserId(folderEntity.getUserId());
        folder.setName(folderEntity.getName());
        folder.setParentId(folderEntity.getParentId());

        folderDao.deleteByFolderId(folder.getFolderId());
    }

    @Override
    public List<Long> findByParentIdList(List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Folder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Folder::getParentId, parentIds)
                .select(Folder::getFolderId); // 只查询 id 提高效率

        return folderDao.selectList(wrapper)
                .stream()
                .map(Folder::getFolderId)
                .filter(Objects::nonNull) // 避免 null
                .collect(Collectors.toList()); // JDK 16+，低版本可用 collect(Collectors.toList())
    }
    //旁路缓存，先读缓存再读数据库，读数据库写缓存
    @Override
    public List<FolderEntity> getFolderList(Long userId) {
        // 先读缓存
        String userFolderListKey = RedisKeyPrefix.USER_FOLDER_LIST.format(userId);

        // 1. 从缓存读取用户文件夹ID列表
        // 获取 Redis List 对象
        RList<Long> rList = redissonClient.getList(userFolderListKey);
        List<Long> folderIds = rList.readAll(); // 获取整个列表
        List<FolderEntity> folderEntityList = new ArrayList<>();
        List<Long> missedFolderIds = new ArrayList<>();

        if (folderIds != null && !folderIds.isEmpty()) {
            for (Long folderId : folderIds) {
                String folderInfoKey = RedisKeyPrefix.FOLDER_INFO.format(folderId);
                RBucket<FolderEntity> bucket = redissonClient.getBucket(folderInfoKey);
                FolderEntity folderEntity = bucket.get(); // 获取对象
                if (folderEntity != null) {
                    folderEntityList.add(folderEntity);
                } else {
                    missedFolderIds.add(folderId);
                }
            }
            if (!missedFolderIds.isEmpty()) {
                LambdaQueryWrapper<Folder> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Folder::getUserId, userId);
                wrapper.in(Folder::getFolderId, missedFolderIds);
                List<Folder> dbFolders = folderDao.selectList(wrapper);

                for (Folder folder : dbFolders) {
                    FolderEntity folderEntity = new FolderEntity();
                    folderEntity.setFolderId(folder.getFolderId());
                    folderEntity.setName(folder.getName());
                    folderEntity.setParentId(folder.getParentId());
                    folderEntityList.add(folderEntity);

                    // 写单个 folder 缓存
                    refreshFolderInfoCache(folder.getFolderId(), folder.getParentId(), folder.getName());
                }
            }
            return folderEntityList;
        }

        // 2. 再读数据库
        LambdaQueryWrapper<Folder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Folder::getUserId, userId);
        List<Folder> folderList=folderDao.selectList(wrapper)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        for (Folder folder : folderList) {
            FolderEntity folderEntity=new FolderEntity();
            folderEntity.setFolderId(folder.getFolderId());
            folderEntity.setName(folder.getName());
            folderEntity.setParentId(folder.getParentId());
            folderEntityList.add(folderEntity);
        }
         // 3. 创建/更新缓存  写缓存
        refreshUserFolderListCache(userId, folderEntityList.stream().map(FolderEntity::getFolderId).collect(Collectors.toList()));
        folderEntityList.forEach(f -> refreshFolderInfoCache(f.getFolderId(), f.getParentId(), f.getName()));

        return folderEntityList;
    }

    /** 创建/更新用户的文件夹列表缓存 */
    private void refreshUserFolderListCache(Long userId, List<Long> folderIds) {
        // 删除旧缓存
        String cacheKey = RedisKeyPrefix.USER_FOLDER_LIST.format(userId);
        redissonClient.getKeys().delete(cacheKey);
        // 写入新缓存
        if (folderIds != null && !folderIds.isEmpty()) {
            redissonClient.getList(cacheKey).addAll(folderIds);
        }
    }

    /** 创建/更新单个文件夹信息缓存 */
    private void refreshFolderInfoCache(Long folderId, Long parentId, String name) {
        String cacheKey = RedisKeyPrefix.FOLDER_INFO.format(folderId);
        redissonClient.getKeys().delete(cacheKey);
        Map<String, Object> info = new HashMap<>();
        info.put("folderId", folderId);
        info.put("parentId", parentId);
        info.put("name", name);
        redissonClient.getMap(cacheKey).putAll(info);
    }

    @Override
    public Optional<FolderEntity> findByFolderIdAndUserId(Long folderId, Long userId) {
        // 1. 查询文件夹
        Folder folder = folderDao.findByFolderIdAndUserId( folderId,  userId);
        // 2. 空值处理 + 转换对象
        return Optional.ofNullable(folder)
                .map(f -> FolderEntity.builder()
                        .folderId(f.getFolderId())
                        .userId(f.getUserId())
                        .parentId(f.getParentId())
                        .name(f.getName())
                        .deleteTime(f.getDeleteTime())
                        .build());
    }

}
