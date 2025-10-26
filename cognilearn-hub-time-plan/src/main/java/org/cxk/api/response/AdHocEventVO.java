package org.cxk.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author KJH
 * @description 突发事件视图对象
 * @create 2025/10/26 09:17
 */
@Data
public class AdHocEventVO {

    private Long eventId;

    private String title;

    private String description;

    private Integer quadrant;

    private String quadrantDesc;

    private Integer energyLevel;

    private BigDecimal durationHours;

    private BigDecimal actualHours;

    private LocalDateTime plannedStartTime;

    private LocalDateTime plannedEndTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private LocalDateTime deadline;

    private Integer status;

    private String statusDesc;

    private Integer priority;

    private String priorityDesc;

    private Long relatedHabitId;

    private List<TagVO> tags;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}