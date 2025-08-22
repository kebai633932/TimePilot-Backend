package org.cxk.api.dto;

import lombok.Data;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/8/19 0:12
 */
@Data
public class FolderNoteDTO {
    private Long folderId;
    private Long parentId;
    private String folderName;
    private List<NoteInfoDTO> notes;
    private List<FolderNoteDTO> folders;
}
