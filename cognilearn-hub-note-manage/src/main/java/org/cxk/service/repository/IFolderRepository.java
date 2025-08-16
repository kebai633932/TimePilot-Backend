package org.cxk.service.repository;

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

    Optional<Folder> findByFolderId(Long folderId);

    void save(Folder folder);

    int countByParentId(Long folderId);

    void delete(Folder folder);

    <T> Optional<T> findByFolderIdAndUserId(Long folderId, Long userId);

    List<Long> findByParentIdList(List<Long> currentLevel);
}
