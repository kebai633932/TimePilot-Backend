package org.cxk.domain;


import org.cxk.api.dto.FolderNoteDTO;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/8/14 11:04
 */
public interface IFolderService {

    Long createFolder(Long userId, String name, Long parentId);

    void deleteFolder(Long userId, Long folderId);

    void updateFolder(Long userId, String name, Long folderId, Long parentId);

    List<FolderNoteDTO> getFolderTree(Long userId);
}
