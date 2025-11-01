package org.cxk.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * @author KJH
 * @description 习惯性事件实体
 * @create 2025/10/26 09:17
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
    /** 事件描述 */
    private String description;
    /** 四象限：1-重要紧急, 2-重要不紧急, 3-紧急不重要, 4-不重要不紧急 */
    private Integer quadrant;
    /** 开始日期 */
    private Instant startDate;
    /** 结束日期 */
    private Instant endDate;
//    /** 持续时间(小时) */
//    private BigDecimal durationHours;
//    /** 优先级：1-低, 2-中, 3-高 */
//    private Integer priority;

//    /** 偏好时间段JSON */
//    private String preferredTimeSlots;
//    /** 重复模式JSON */
//    private String repeatPattern;
//    /** 状态：1-启用, 0-停用 */
//    private Integer status;
//    /** 完成率百分比 */
//    private BigDecimal completionRate;
//    /** 标签列表 */
//    private List<TagEntity> tags;
}