package org.cxk.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author KJH
 * @description 突发事件更新请求 DTO
 * @create 2025/10/26 09:17
 */
@Data
public class AdHocEventUpdateDTO {

    /** 事件主键ID */
    @NotNull(message = "事件ID不能为空")
    private Long id;

    /** 事件标题 */
    @NotBlank(message = "事件标题不能为空")
    @Size(max = 200, message = "事件标题长度不能超过200个字符")
    private String title;

    /** 四象限分类：1-重要紧急, 2-重要不紧急, 3-紧急不重要, 4-不重要不紧急 */
    @NotNull(message = "四象限分类不能为空")
    @Min(value = 1, message = "四象限值必须在1到4之间")
    @Max(value = 4, message = "四象限值必须在1到4之间")
    private Integer quadrant;

    /** 计划开始时间 */
    @NotNull(message = "计划开始时间不能为空")
    private Instant plannedStartTime;

    /** 计划结束时间 */
    @NotNull(message = "计划结束时间不能为空")
    private Instant plannedEndTime;

    /** 截止时间（DDL，可选） */
    private Instant deadline;

    /** 事件描述（可选） */
    @Size(max = 1000, message = "事件描述长度不能超过1000个字符")
    private String description;

    /** 计量单位（如：分钟、页、次、个等，可选） */
    @Size(max = 20, message = "计量单位长度不能超过20个字符")
    private String measurementUnit;
    /** 已完成数量 */
    @DecimalMin(value = "0.0", inclusive = false, message = "目标数量必须大于0")
    @Digits(integer = 10, fraction = 2, message = "目标数量格式不正确")
    private BigDecimal completedQuantity;
    /** 目标数量（例如：读10页书、写1000字，可选） */
    @DecimalMin(value = "0.0", inclusive = false, message = "目标数量必须大于0")
    @Digits(integer = 10, fraction = 2, message = "目标数量格式不正确")
    private BigDecimal targetQuantity;

    /** 初始状态（默认：1-未完成） */
    private Integer status = 1;
}
