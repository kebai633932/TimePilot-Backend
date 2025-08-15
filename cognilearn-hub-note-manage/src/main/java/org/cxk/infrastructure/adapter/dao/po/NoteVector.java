package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 笔记向量表
 */
@Data
@TableName("note_vector")
public class NoteVector {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;
    private Long tenantId;

    private float[] embedding; // pgvector 可映射为 float[]

    @TableLogic
    private Boolean isDeleted;

    private Date deleteTime;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
