package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

/**
 * @author KJH
 * @description 习惯性事件实体
 * @create 2025/10/26 09:17
 */
@Data
@TableName("habitual_events")
public class HabitualEvent {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 事件标题 */
    private String title;

    /** 事件描述 */
    private String description;

    /** 象限分类：1-重要紧急，2-重要不紧急，3-紧急不重要，4-不重要不紧急 */
    private Integer quadrant;

    /** 预计花费时间（小时） */
    private BigDecimal estimatedTime;

    /** 偏好时间段（JSON），例如 [{"start": "06:00", "end": "07:00", "days": [1,2,3,4,5]}] */
    private String preferredTimeSlots;

    /** 重复模式：daily、weekly、monthly、custom */
    private String repeatPattern;

    /** 重复间隔 */
    private Integer repeatInterval;

    /** 完成率百分比 */
    private BigDecimal completionRate;

    /** 计量单位，如分钟、页、次、个等 */
    private String measurementUnit;

    /** 目标数量 */
    private BigDecimal targetQuantity;

    /** 已完成数量 */
    private BigDecimal completedQuantity;

    /** 逻辑删除 */
    @TableLogic
    private Boolean isDeleted;

    /** 删除时间 */
    private Date deleteTime;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}