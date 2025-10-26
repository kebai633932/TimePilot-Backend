package org.cxk.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author KJH
 * @description 习惯性事件视图对象
 * @create 2025/10/26 09:17
 */
@Data
public class HabitualEventVO {

    private Long eventId;

    private String title;

    private String description;

    private Integer quadrant;

    private String quadrantDesc;

    private Integer energyLevel;

    private BigDecimal durationHours;

    private Integer priority;

    private String priorityDesc;

    private LocalDate startDate;

    private LocalDate endDate;

    private String preferredTimeSlots;

    private String repeatPattern;

    private String repeatPatternDesc;

    private Integer status;

    private String statusDesc;

    private BigDecimal completionRate;

    private List<TagVO> tags;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // 计算字段
    private Boolean isExpired;

    private Boolean isActiveToday;

    private String progressStatus;
}