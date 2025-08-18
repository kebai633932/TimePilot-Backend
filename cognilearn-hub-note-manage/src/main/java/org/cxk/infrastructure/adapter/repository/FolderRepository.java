package org.cxk.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.cxk.domain.model.entity.FolderEntity;
import org.cxk.infrastructure.adapter.dao.IFolderDao;
import org.cxk.infrastructure.adapter.dao.po.Folder;
import org.cxk.domain.repository.IFolderRepository;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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


    @Override
    public Optional<FolderEntity> findByFolderIdAndUserId(Long folderId, Long userId) {
        // 1. 查询文件夹
        LambdaQueryWrapper<Folder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Folder::getFolderId, folderId)
                .eq(Folder::getUserId, userId)
                .last("LIMIT 1");
        Folder folder = folderDao.selectOne(wrapper);
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
