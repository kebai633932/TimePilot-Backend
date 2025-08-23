package org.cxk.api.dto;

import lombok.Data;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/8/21 18:10
 */
@Data
public class VectorSearchResponseDTO {
    private List<NoteInfo> noteInfoList;


    @Data
    public static class NoteInfo {

        private Long noteId;  // 笔记id

        private String title;    // 笔记标题
    }
}
