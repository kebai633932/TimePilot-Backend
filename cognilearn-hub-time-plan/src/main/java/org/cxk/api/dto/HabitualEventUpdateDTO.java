package org.cxk.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author KJH
 * @description 习惯性事件更新DTO
 * @create 2025/10/26 09:17
 */
@Data
public class HabitualEventUpdateDTO {

    /** 事件ID */
    @NotNull(message = "事件ID不能为空")
    private Long eventId;

    /** 事件标题 */
    @NotBlank(message = "事件标题不能为空")
    @Size(max = 200, message = "事件标题长度不能超过200个字符")
    private String title;

    /** 象限分类：1-重要紧急，2-重要不紧急，3-紧急不重要，4-不重要不紧急 */
    @NotNull(message = "四象限不能为空")
    @Min(value = 1, message = "四象限值必须在1-4之间")
    @Max(value = 4, message = "四象限值必须在1-4之间")
    private Integer quadrant;


    /** 事件描述 */
    @Size(max = 1000, message = "事件描述长度不能超过1000个字符")
    private String description;
    /** 预计花费时间（小时） */
    @DecimalMin(value = "0.0", inclusive = false, message = "预计时间必须大于0")
    private BigDecimal estimatedTime;

    /** 偏好时间段（JSON） */
    @Size(max = 2000, message = "偏好时间段字段过长")
    private String preferredTimeSlots;

    /** 重复模式：daily、weekly、monthly、custom */
    @Size(max = 50, message = "重复模式长度不能超过50个字符")
    private String repeatPattern;

    /** 重复间隔 */
    @Min(value = 1, message = "重复间隔至少为1")
    private Integer repeatInterval;

    /** 计量单位 */
    @Size(max = 50, message = "计量单位长度不能超过50个字符")
    private String measurementUnit;

    /** 目标数量 */
    @DecimalMin(value = "0.0", message = "目标数量不能为负")
    private BigDecimal targetQuantity;

    /** 已完成数量 */
    @DecimalMin(value = "0.0", message = "已完成数量不能为负")
    private BigDecimal completedQuantity;
}
