package org.cxk.domain;

/**
 * @author KJH
 * @description
 * @create 2025/8/14 11:04
 */
public interface IFolderDomainService {

    Long createFolder(Long userId, String name, Long parentId);

    void deleteFolder(Long userId, Long folderId);

    void updateFolder(Long userId, String name, Long folderId, Long parentId);
}
