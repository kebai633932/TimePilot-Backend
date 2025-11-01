package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

/**
 * @author KJH
 * @description 突发性事件实体
 * @create 2025/10/26 09:17
 */
@Data
@TableName("ad_hoc_events")
public class AdHocEvent {
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


    /** 预计耗时(小时) */
    private BigDecimal durationHours;

    /** 实际耗时(小时) */
    private BigDecimal actualHours;

    /** 计划开始时间 */
    private Instant plannedStartTime;

    /** 计划结束时间 */
    private Instant plannedEndTime;

    /** 实际开始时间 */
    private Instant actualStartTime;

    /** 实际结束时间 */
    private Instant actualEndTime;

    /** 截止时间 */
    private Instant deadline;

    /** 状态：1-待开始, 2-进行中, 3-已完成, 4-已取消 */
    private Integer status;

    /** 优先级：1-低, 2-中, 3-高, 4-紧急 */
    private Integer priority;

    /** 关联的习惯性事件ID */
    private Long relatedHabitId;

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