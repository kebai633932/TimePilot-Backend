//package org.cxk.api.response;
//
//import lombok.Data;
//
//import java.time.LocalDate;
//import java.util.List;
//
///**
// * @author KJH
// * @description 每日时间安排视图对象
// * @create 2025/10/26 12:58
// */
//@Data
//public class DailyTimePlanVO {
//
//    private LocalDate date;
//
//    private String dayOfWeek;
//
//    /**
//     * 当日总事件数
//     */
//    private Integer totalEventCount;
//
//    /**
//     * 当日总预计时长（小时）
//     */
//    private java.math.BigDecimal totalEstimatedHours;
//
//    /**
//     * 当日事件列表
//     */
//    private List<TimePlanEventVO> events;
//
//}