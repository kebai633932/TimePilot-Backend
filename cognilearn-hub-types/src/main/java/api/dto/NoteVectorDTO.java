package api.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author KJH
 * @description
 * @create 2025/8/22 14:27
 */
@Data
public class NoteVectorDTO {

    /** 笔记ID */
    private Long noteId;
    private Long userId;
    private Long folderId;
    private String title;
    private String contentPlain;
    private String contentMd;
    private Boolean isDeleted;

    private Date deleteTime;
    private Long version;
}
