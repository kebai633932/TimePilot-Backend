package org.cxk.api.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * @author KJH
 * @description 习惯性事件查询DTO
 * @create 2025/10/26 09:17
 */
@Data
public class HabitualEventQueryDTO {

    private Integer status;

    private Integer quadrant;

    private Integer priority;

    private Integer energyLevel;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<Long> tagIds;

    private String keyword;

    private Boolean includeExpired = false;

    private Boolean includeInactive = false;

    private Integer pageNum = 1;

    private Integer pageSize = 20;

    private String sortField = "createTime";

    private String sortOrder = "DESC";
}