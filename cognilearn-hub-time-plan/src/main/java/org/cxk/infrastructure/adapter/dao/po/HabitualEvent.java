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

    /** 四象限：1-重要紧急, 2-重要不紧急, 3-紧急不重要, 4-不重要不紧急 */
    private Integer quadrant;

    /** 持续时间(小时) */
    private BigDecimal durationHours;

    /** 优先级：1-低, 2-中, 3-高 */
    private Integer priority;

    /** 开始日期 */
    private Instant startDate;

    /** 结束日期 */
    private Instant endDate;

    /** 偏好时间段JSON */
    private String preferredTimeSlots;

    /** 重复模式JSON */
    private String repeatPattern;

    /** 状态：1-启用, 0-停用 */
    private Integer status;

    /** 完成率百分比 */
    private BigDecimal completionRate;

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