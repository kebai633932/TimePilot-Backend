package org.cxk.service;

import org.cxk.infrastructure.adapter.dao.po.Folder;

/**
 * @author KJH
 * @description
 * @create 2025/8/14 11:04
 */
public interface IFolderService {

    Long createFolder(Long userId, String name, Long parentId);

    void deleteFolder(Long userId, Long folderId);

    void updateFolder(Long userId, String name, Long folderId, Long parentId);
}
