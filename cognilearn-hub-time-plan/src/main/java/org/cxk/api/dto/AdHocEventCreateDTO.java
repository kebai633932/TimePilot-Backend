package org.cxk.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Instant;

/**
 * @author KJH
 * @description 突发事件创建DTO
 * @create 2025/10/26 09:17
 */
@Data
public class AdHocEventCreateDTO {

    @NotBlank(message = "事件标题不能为空")
    @Size(max = 200, message = "事件标题长度不能超过200个字符")
    private String title;

    @NotNull(message = "四象限不能为空")
    @Min(value = 1, message = "四象限值必须在1-4之间")
    @Max(value = 4, message = "四象限值必须在1-4之间")
    private Integer quadrant;
    @NotBlank()
    private Instant plannedStartTime;
    @NotBlank()
    private Instant plannedEndTime;
}