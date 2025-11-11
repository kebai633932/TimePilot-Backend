package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

/**
 * @author KJH
 * @description 突发性事件实体（一次性任务或临时计划）
 * @create 2025/10/23
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

    /** 计划开始时间 */
    private Instant plannedStartTime;

    /** 计划结束时间 */
    private Instant plannedEndTime;

    /** 截止时间（DDL） */
    private Instant deadline;

    /** 状态：1-未完成，2-已完成，3-已延期，4-已取消 */
    private Integer status;

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