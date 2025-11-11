package org.cxk.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * @author KJH
 * @description 习惯性事件删除DTO
 * @create 2025/10/26 09:17
 */
@Data
public class HabitualEventDeleteDTO {

    @NotNull(message = "事件ID不能为空")
    private Long id;
}