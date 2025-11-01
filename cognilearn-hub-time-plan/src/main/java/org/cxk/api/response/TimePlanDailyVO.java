//package org.cxk.api.response;
//
//import lombok.Data;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//
///**
// * @author KJH
// * @description 周时间安排视图对象
// * @create 2025/10/26 12:58
// */
//@Data
//public class TimePlanDailyVO {
//
//    /**
//     * 查询开始日期（周一）
//     */
//    private LocalDate startDate;
//
//    /**
//     * 查询结束日期（周日）
//     */
//    private LocalDate endDate;
//
//    /**
//     * 每日时间安排列表（周一到周日）
//     */
//    private List<DailyTimePlanVO> dailyPlans;
//
//    /**
//     * 总事件数
//     */
//    private Integer totalEventCount;
//
//    /**
//     * 总预计时长（小时）
//     */
//    private BigDecimal totalEstimatedHours;
//
//    /**
//     * 已完成事件数
//     */
//    private Integer completedEventCount;
//
//    /**
//     * 完成率
//     */
//    private BigDecimal completionRate;
//
//    /**
//     * 习惯性事件数量
//     */
//    private Integer habitualEventCount;
//
//    /**
//     * 突发性事件数量
//     */
//    private Integer adHocEventCount;
//
//    /**
//     * 是否有时间冲突
//     */
//    private Boolean hasTimeConflicts;
//}