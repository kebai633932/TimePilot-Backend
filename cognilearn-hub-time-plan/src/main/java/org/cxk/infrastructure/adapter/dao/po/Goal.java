package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;
import java.util.Date;

/**
 * @author KJH
 * @description 目标实体（支持长期、中期、短期目标的分层结构）
 * @create 2025/11/14
 */
@Data
@TableName("goals")
public class Goal {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String description;

    /** 目标类型：1-长期，2-中期，3-短期 */
    private Integer goalType;

    /** 目标领域 */
    private String category;

    /** 目标截止时间 */
    private Instant deadline;

    @TableLogic
    private Boolean isDeleted;

    private Date deleteTime;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}

