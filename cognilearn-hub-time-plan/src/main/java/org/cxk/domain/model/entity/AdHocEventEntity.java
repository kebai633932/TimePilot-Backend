package org.cxk.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author KJH
 * @description 突发性事件实体
 * @create 2025/10/26 09:17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdHocEventEntity {
    /** 事件ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 事件标题 */
    private String title;
    /** 事件描述 */
    private String description;
    /** 四象限：1-重要紧急, 2-重要不紧急, 3-紧急不重要, 4-不重要不紧急 */
    private Integer quadrant;
    /** 精力消耗等级：1-5级 */
    private Integer energyLevel;
    /** 预计耗时(小时) */
    private BigDecimal durationHours;
    /** 实际耗时(小时) */
    private BigDecimal actualHours;
    /** 计划开始时间 */
    private Date plannedStartTime;
    /** 计划结束时间 */
    private Date plannedEndTime;
    /** 实际开始时间 */
    private Date actualStartTime;
    /** 实际结束时间 */
    private Date actualEndTime;
    /** 截止时间 */
    private Date deadline;
    /** 状态：1-待开始, 2-进行中, 3-已完成, 4-已取消 */
    private Integer status;
    /** 优先级：1-低, 2-中, 3-高, 4-紧急 */
    private Integer priority;
    /** 关联的习惯性事件ID */
    private Long relatedHabitId;
    /** 标签列表 */
    private List<TagEntity> tags;
}