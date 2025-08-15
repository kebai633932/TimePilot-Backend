package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 笔记附件表
 */
@Data
@TableName("note_attachment")
public class NoteAttachment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;

    @TableLogic
    private Boolean isDeleted;

    private Date deleteTime;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
