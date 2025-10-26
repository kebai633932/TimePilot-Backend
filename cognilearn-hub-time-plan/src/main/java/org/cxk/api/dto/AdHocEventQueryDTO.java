package org.cxk.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author KJH
 * @description 突发事件查询DTO
 * @create 2025/10/26 09:17
 */
@Data
public class AdHocEventQueryDTO {

    private Integer status;

    private Integer quadrant;

    private Integer priority;

    private Integer energyLevel;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime deadlineStart;

    private LocalDateTime deadlineEnd;

    private List<Long> tagIds;

    private String keyword;

    private Integer pageNum = 1;

    private Integer pageSize = 20;

    private String sortField = "createTime";

    private String sortOrder = "DESC";
}