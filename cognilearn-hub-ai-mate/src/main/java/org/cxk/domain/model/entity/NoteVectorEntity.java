package org.cxk.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * @author KJH
 * @description
 * @create 2025/8/18 15:44
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteVectorEntity {

    /** 笔记ID */
    private Long noteId;
    private Long userId;
    private Long folderId;
    private String title;
    private String contentPlain;
    private Boolean isDeleted;
    Map<String, Object> metadata;

    private Date deleteTime;
    private Long version;
    private float[] embedding;
}
