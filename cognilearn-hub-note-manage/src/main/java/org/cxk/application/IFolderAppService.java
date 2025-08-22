package org.cxk.application;

import org.cxk.api.dto.FolderNoteDTO;
import org.cxk.api.dto.NoteInfoDTO;

import java.util.List;
import java.util.Map;

/**
 * @author KJH
 * @description 负责文件夹领域
 * @create 2025/8/14 11:04
 */
public interface IFolderAppService {

    Long createFolder(Long userId, String name, Long parentId);

    void deleteFolder(Long userId, Long folderId);

    void updateFolder(Long userId, String name, Long folderId, Long parentId);

    Map<Long, FolderNoteDTO> getFolderMap(Long userId) ;

    FolderNoteDTO buildFolderTree(Map<Long, FolderNoteDTO> folderNoteDTOMap,List<NoteInfoDTO> rootNotes);
}
