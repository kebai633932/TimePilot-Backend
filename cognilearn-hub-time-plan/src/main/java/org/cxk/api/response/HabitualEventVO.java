package org.cxk.api.response;

import lombok.Data;

import java.time.Instant;

/**
 * @author KJH
 * @description 习惯性事件视图对象
 * @create 2025/10/26 09:17
 */
@Data
public class HabitualEventVO {

    private Long eventId;

    private String title;

    private Integer quadrant;

    private Instant startTime;

    private Instant endTime;
}