package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

/**
 * TODO
 * 有限习惯任务实体（Finite Habit Task）
 * 用于表示有周期但有截止的目标，比如：
 * “30天读完一本书，每天10页”
 */
@Data
@TableName("finite_habit_tasks")
public class FiniteHabitTask {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 任务标题 */
    private String title;

    /** 任务描述 */
    private String description;

    /** 任务类型（如：阅读、运动、学习等） */
    private String category;

    /** 周期单位：daily / weekly / custom */
    private String cycleType;

    /** 每周期目标数量（如：每天10页、每周3次） */
    private BigDecimal targetPerCycle;

    /** 计量单位（页、次、分钟等） */
    private String measurementUnit;

    /** 总周期数（如30天、4周、21天等） */
    private Integer totalCycles;

    /** 当前已完成周期数 */
    private Integer completedCycles;

    /** 任务总目标量（如300页） */
    private BigDecimal totalTargetQuantity;

    /** 当前已完成量 */
    private BigDecimal completedQuantity;

    /** 计划开始时间 */
    private Instant startTime;

    /** 计划结束时间 */
    private Instant endTime;

    /** 状态：1-未开始，2-进行中，3-已完成，4-暂停，5-放弃 */
    private Integer status;

    /** 完成率百分比 */
    private BigDecimal completionRate;

    /** 是否允许自动延长周期（如未完成顺延1天） */
    private Boolean autoPostpone;

    /** 逻辑删除 */
    @TableLogic
    private Boolean isDeleted;

    /** 删除时间 */
    private Date deleteTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
