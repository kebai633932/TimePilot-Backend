//package org.cxk.api.response;
//
//import lombok.Data;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
///**
// * @author KJH
// * @description 时间安排事件视图对象
// * @create 2025/10/26 12:58
// */
//@Data
//public class TimePlanEventVO {
//
//    private Long eventId;
//
//    private String eventType; // "HABITUAL" 或 "AD_HOC"
//
//    private String title;
//
//    private Integer quadrant;
//
//    private String quadrantDesc;
//
//    private Integer energyLevel;
//
//    private Integer priority;
//
//    private String priorityDesc;
//
//    private BigDecimal durationHours;
//
//    private String status;
//
//    private String statusDesc;
//
//    private LocalDateTime startTime;
//
//    private LocalDateTime endTime;
//
//
//    /**
//     * 是否有时段冲突
//     */
//    private Boolean hasTimeConflict;
//}