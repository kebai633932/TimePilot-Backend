package org.cxk.api.dto;

import lombok.Data;

/**
 * @author KJH
 * @description
 * @create 2025/8/19 11:58
 */
@Data
public class NoteInfoDTO {
    private String title;
    private Long folderId;
    private Long noteId;
}
