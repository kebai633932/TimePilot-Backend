package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * TODO
 * @author KJH
 * @description
 * @create 2025/11/14
 */
@Data
@TableName("goal_action_event_links")
public class GoalEventLink {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** action 表主键 */
    private Long goalActionId;

    /** 事件类型：1-AdHocEvent, 2-HabitualEvent, 3-FiniteHabitTask */
    private Integer eventType;

    /** 真正的事件ID */
    private Long eventId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
