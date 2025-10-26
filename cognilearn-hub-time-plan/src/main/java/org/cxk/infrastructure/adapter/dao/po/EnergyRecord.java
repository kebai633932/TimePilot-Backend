package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author KJH
 * @description 精力分配记录实体
 * @create 2025/10/26 09:17
 */
@Data
@TableName("energy_records")
public class EnergyRecord {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 记录日期 */
    private Date recordDate;

    /** 总精力水平：1-5级 */
    private Integer totalEnergyLevel;

    /** 精力状况备注 */
    private String notes;

    /** 时间段精力分布JSON */
    private String timeSlotEnergy;

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