package org.cxk.api.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author KJH
 * @description 事件状态更新DTO
 * @create 2025/10/26 09:17
 */
@Data
public class EventStatusUpdateDTO {

    @NotNull(message = "事件ID不能为空")
    private Long eventId;

    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态值必须在1-4之间")
    @Max(value = 4, message = "状态值必须在1-4之间")
    private Integer status;
}