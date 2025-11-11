package org.cxk.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author KJH
 * @description 习惯性事件视图对象（用于前端展示）
 * @create 2025/10/26
 */
@Data
public class HabitualEventVO {

    /** 事件ID */
    private Long id;

    /** 用户ID（可选，通常在管理端接口返回） */
    private Long userId;

    /** 事件标题 */
    private String title;

    /** 象限分类：1-重要紧急，2-重要不紧急，3-紧急不重要，4-不重要不紧急 */
    private Integer quadrant;

    /** 预计花费时间（小时） */
    private BigDecimal estimatedTime;

    /** 事件描述 */
    private String description;

    /** 偏好时间段（JSON 字符串或结构化字段） */
    private String preferredTimeSlots;

    /** 重复模式：daily、weekly、monthly、custom */
    private String repeatPattern;

    /** 重复间隔 */
    private Integer repeatInterval;

    /** 完成率百分比 */
    private BigDecimal completionRate;

    /** 计量单位（如 "次"、"页"、"公里"） */
    private String measurementUnit;

    /** 目标数量 */
    private BigDecimal targetQuantity;

    /** 已完成数量 */
    private BigDecimal completedQuantity;

    /** 开始时间（例如当前周期的开始时间） */
    private Instant startTime;

    /** 结束时间（例如当前周期的结束时间） */
    private Instant endTime;
}