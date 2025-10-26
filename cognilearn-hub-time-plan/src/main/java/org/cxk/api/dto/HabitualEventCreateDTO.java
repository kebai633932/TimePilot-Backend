package org.cxk.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.*;
import lombok.Data;
/**
 * @author KJH
 * @description 习惯性事件创建DTO
 * @create 2025/10/26 09:17
 */
@Data
public class HabitualEventCreateDTO {

    @NotBlank(message = "事件标题不能为空")
    @Size(max = 200, message = "事件标题长度不能超过200个字符")
    private String title;

    @Size(max = 1000, message = "事件描述长度不能超过1000个字符")
    private String description;

    @NotNull(message = "四象限不能为空")
    @Min(value = 1, message = "四象限值必须在1-4之间")
    @Max(value = 4, message = "四象限值必须在1-4之间")
    private Integer quadrant;

    @NotNull(message = "精力等级不能为空")
    @Min(value = 1, message = "精力等级必须在1-5之间")
    @Max(value = 5, message = "精力等级必须在1-5之间")
    private Integer energyLevel;

    @NotNull(message = "持续时间不能为空")
    @DecimalMin(value = "0.1", message = "持续时间必须大于0")
    @DecimalMax(value = "24.0", message = "持续时间不能超过24小时")
    private BigDecimal durationHours;

    @NotNull(message = "优先级不能为空")
    @Min(value = 1, message = "优先级必须在1-3之间")
    @Max(value = 3, message = "优先级必须在1-3之间")
    private Integer priority;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    private LocalDate endDate;

    private String preferredTimeSlots;

    private String repeatPattern;

    private List<Long> tagIds;
}