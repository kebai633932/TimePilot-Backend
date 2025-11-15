package org.cxk.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

/**
 * @author KJH
 * @description
 * @create 2025/11/2 08:58
 */
@Data
public class SmartDailyPlanGenerateDTO {
    @NotNull(message = "查询日期不能为空")
    private Instant date;
    @NotNull(message = "客户端地区不能为空")
    private String clientTimeZone;
}
