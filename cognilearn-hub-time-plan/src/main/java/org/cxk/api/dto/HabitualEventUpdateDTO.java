package org.cxk.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Date;

/**
 * @author KJH
 * @description 习惯性事件更新DTO
 * @create 2025/10/26 09:17
 */
@Data
public class HabitualEventUpdateDTO {

    @NotNull(message = "事件ID不能为空")
    private Long eventId;

    @NotBlank(message = "事件标题不能为空")
    @Size(max = 200, message = "事件标题长度不能超过200个字符")
    private String title;

    @Size(max = 1000, message = "事件描述长度不能超过1000个字符")
    private String description;

    @NotNull(message = "四象限不能为空")
    @Min(value = 1, message = "四象限值必须在1-4之间")
    @Max(value = 4, message = "四象限值必须在1-4之间")
    private Integer quadrant;

    @NotNull(message = "开始日期不能为空")
    private Date startDate;

    @NotNull(message = "结束日期不能为空")
    private Date endDate;
}