//package org.cxk.trigger.job.scheduler;
//
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.cxk.domain.IAdHocEventService;
//import org.cxk.domain.IHabitualEventService;
//import org.cxk.domain.IUserAuthService;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.concurrent.*;
//
///**
// * @author KJH
// * @description 时间计划定时任务
// * @create 2025/10/26 14:00
// */
//@Slf4j
//@Component
//@AllArgsConstructor
//public class TimePlanScheduler {
//
//    private final IHabitualEventService habitualEventService;
//    private final IAdHocEventService adHocEventService;
//    private final IUserAuthService userAuthService;
//
//    // 使用ThreadPoolExecutor创建线程池,io密集,需要测压
//    //数据库读取（IO等待）
//    //调用OpenAI API（网络IO等待）
//    //数据库写入（IO等待）
//    private final Executor userPlanExecutor = new ThreadPoolExecutor(
//            // 核心线程数：CPU核心数的2-4倍
//            Runtime.getRuntime().availableProcessors() * 3,
//
//            // 最大线程数：核心线程数的1.5-2倍
//            Runtime.getRuntime().availableProcessors() * 5,
//
//            // 空闲时间：较短，因为任务可能批量到达
//            30L, TimeUnit.SECONDS,
//
//            // 工作队列：较大容量，应对OpenAI API调用延迟
//            new LinkedBlockingQueue<>(200),
//
//            // 拒绝策略：保证重要任务不丢失
//            new ThreadPoolExecutor.CallerRunsPolicy()
//    );
//
//    /**
//     * 每天凌晨2点生成明天的计划
//     */
//    @Scheduled(cron = "0 0 2 * * ?")
//    public void generateDailyPlan() {
//        try {
//            log.info("开始执行每日计划生成任务");
//
//            // 获取所有需要生成计划的用户
//            List<Long> userIds = userAuthService.getAllActiveUserIds();
//
//            // 使用CompletableFuture实现用户级并发
//            List<CompletableFuture<Void>> futures = userIds.stream()
//                    .map(userId -> CompletableFuture
//                            .runAsync(() -> generateUserDailyPlan(userId), userPlanExecutor)
//                    )
//                    .collect(Collectors.toList());
//
//            // 等待所有任务完成
//            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//
//            log.info("每日计划生成任务执行完成，共处理 {} 个用户", userIds.size());
//        } catch (Exception e) {
//            log.error("执行每日计划生成任务失败", e);
//        }
//    }
//
//    /**
//     * 为用户生成每日计划
//     */
//    private void generateUserDailyPlan(Long userId) {
//        try {
//            LocalDate tomorrow = LocalDate.now().plusDays(1);
//
//            // 1. 读取日常计划
//            List<Object> habitualEvents = habitualEventService.getHabitualEventsByDate(userId, LocalDate.now());
//
//            // 2. 读取突发事件
//            List<Object> adHocEvents = adHocEventService.getAdHocEventsByDate(userId, LocalDate.now());
//
//            // 3. 调用AI服务生成计划
//            Object aiResponse = callAIService(userId, tomorrow, habitualEvents, adHocEvents);
//
//            // 4. 保存计划到数据库
//            saveDailyPlanToDatabase(userId, tomorrow, aiResponse);
//
//            log.info("成功为用户生成每日计划，userId={}, date={}", userId, tomorrow);
//        } catch (Exception e) {
//            log.error("为用户生成每日计划失败，userId={}", userId, e);
//        }
//    }
//
//    /**
//     * 调用AI服务生成计划
//     */
//    private Object callAIService(Long userId, LocalDate date, List<Object> habitualEvents, List<Object> adHocEvents) {
//        // TODO: 实现AI服务调用
//        return new Object();
//    }
//
//    /**
//     * 保存每日计划到数据库
//     */
//    private void saveDailyPlanToDatabase(Long userId, LocalDate date, Object aiResponse) {
//        // TODO: 实现数据库保存
//    }
//}