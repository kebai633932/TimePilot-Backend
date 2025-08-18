package org.cxk.domain.repository;

import org.cxk.domain.model.entity.FolderEntity;
import org.cxk.infrastructure.adapter.dao.po.Folder;

import java.util.List;
import java.util.Optional;

/**
 * @author KJH
 * @description
 * @create 2025/8/14 15:26
 */
public interface IFolderRepository {
    boolean existsByUserIdAndParentIdAndName(Long userId, Long parentId, String name);

    Optional<FolderEntity> findByFolderId(Long folderId);

    void save(FolderEntity folderEntity);

    int countByParentId(Long folderId);

    void delete(FolderEntity folderEntity);

    Optional<FolderEntity> findByFolderIdAndUserId(Long folderId, Long userId);

    List<Long> findByParentIdList(List<Long> currentLevel);
}
