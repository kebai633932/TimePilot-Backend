package org.cxk.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author KJH
 * @description 精力分配记录实体
 * @create 2025/10/26 09:17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnergyRecordEntity {
    /** 记录ID */
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
}