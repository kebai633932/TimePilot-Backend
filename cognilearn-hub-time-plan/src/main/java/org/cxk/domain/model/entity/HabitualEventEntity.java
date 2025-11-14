package org.cxk.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author KJH
 * @description 习惯性事件实体（领域层）
 * @create 2025/10/26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HabitualEventEntity {

    /** 事件ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 事件标题 */
    private String title;

    /** 象限分类：1-重要紧急，2-重要不紧急，3-紧急不重要，4-不重要不紧急 */
    private Integer quadrant;

    /** 预计花费时间（小时） */
    private BigDecimal estimatedTime;

    /** 重复模式：daily、weekly、monthly、custom */
    private String repeatPattern;

    /** 重复间隔 */
    private Integer repeatInterval;

    /** ===================== 🌱 下面为可选部分 ===================== */

    /** 事件描述 */
    private String description;

    /** 偏好时间段（JSON） */
    private String preferredTimeSlots;

    /** 完成率百分比 */
    private BigDecimal completionRate;

    /** 计量单位 */
    private String measurementUnit;

    /** 目标数量 */
    private BigDecimal targetQuantity;

    /** 已完成数量 */
    private BigDecimal completedQuantity;

}