package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 笔记表
 */
@Data
@TableName("note")
public class Note {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long folderId;
    private String title;
    private String contentMd;
    private String contentPlain;
    private Short status; // 0 草稿 / 1 发布 / 2 删除
    private Boolean isPublic;

    @TableLogic
    private Boolean isDeleted;

    private Date deleteTime;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}