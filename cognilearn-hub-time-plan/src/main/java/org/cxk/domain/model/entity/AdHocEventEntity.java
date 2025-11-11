package org.cxk.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author KJH
 * @description 领域层：突发性事件实体（临时任务或一次性计划）
 * @create 2025/10/26 09:17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdHocEventEntity {

    /** 唯一标识 */
    private Long id;

    /** 所属用户 */
    private Long userId;

    /** 标题 */
    private String title;

    /** 四象限：1-重要紧急, 2-重要不紧急, 3-紧急不重要, 4-不重要不紧急 */
    private Integer quadrant;

    /** 计划开始时间 */
    private Instant plannedStartTime;

    /** 计划结束时间 */
    private Instant plannedEndTime;

    /** ===================== 🌱 下面为可选部分 ===================== */
    /** 截止时间 */
    private Instant deadline;

    /** 描述（可选） */
    private String description;

    /** 状态：（默认）1-未完成，2-已完成，3-已延期，4-已取消 */
    private Integer status;

    /** 计量单位（如：页、次、个） */
    private String measurementUnit;

    /** 目标数量 */
    private BigDecimal targetQuantity;

    /** 已完成数量 */
    private BigDecimal completedQuantity;
}