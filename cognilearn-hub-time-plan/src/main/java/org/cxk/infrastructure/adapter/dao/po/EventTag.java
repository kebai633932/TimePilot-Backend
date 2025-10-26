package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author KJH
 * @description 事件标签关联实体
 * @create 2025/10/26 09:17
 */
@Data
@TableName("event_tags")
public class EventTag {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 事件类型：1-习惯性事件, 2-突发性事件 */
    private Integer eventType;

    /** 事件ID */
    private Long eventId;

    /** 标签ID */
    private Long tagId;

    /** 逻辑删除 */
    @TableLogic
    private Boolean isDeleted;

    /** 删除时间 */
    private Date deleteTime;

    /** 创建时间，自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新时间，自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}