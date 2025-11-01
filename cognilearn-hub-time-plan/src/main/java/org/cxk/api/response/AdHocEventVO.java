package org.cxk.api.response;

import lombok.Data;

import java.time.Instant;

/**
 * @author KJH
 * @description 突发事件视图对象
 * @create 2025/10/26 09:17
 */
@Data
public class AdHocEventVO {

    private Long eventId;

    private String title;

    private Integer quadrant;

    private Instant plannedStartTime;

    private Instant plannedEndTime;
}