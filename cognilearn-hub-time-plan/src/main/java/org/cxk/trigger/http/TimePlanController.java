package org.cxk.trigger.http;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.cxk.api.dto.SmartDailyPlanGenerateDTO;
import org.cxk.api.response.Response;
import org.cxk.domain.IAdHocEventService;
import org.cxk.domain.IHabitualEventService;
import org.cxk.domain.model.entity.AdHocEventEntity;
import org.cxk.domain.model.entity.HabitualEventEntity;
import org.cxk.types.enums.ResponseCode;
import org.cxk.util.AuthenticationUtil;
import org.cxk.util.ClientDateTimeUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 智能规划每日时间安排
 */
@Slf4j
@RestController
@RequestMapping("/api/time-plan")
@AllArgsConstructor
public class TimePlanController {

    private final IAdHocEventService adHocEventService;
    private final IHabitualEventService habitualEventService;
    /**
     * todo 冲突时显示什么呢？
     * 智能规划当日时间安排
     */
    @PostMapping("/smart-daily-plan")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<?> generateSmartDailyPlan(@Valid @RequestBody SmartDailyPlanGenerateDTO dto) {

        Long userId = AuthenticationUtil.getCurrentUserId();
        try {
            // 1. 获取事件
            List<AdHocEventEntity> adHocEvents = adHocEventService.getTodayEvents(userId, dto.getDate(),dto.getClientTimeZone());
            List<HabitualEventEntity> habitualEvents = habitualEventService.listUserHabitualEventEntitys(userId);
            List<TimeRange> forbiddenSlots = new ArrayList<>();

            Instant dayStart = ClientDateTimeUtils.getClientDayStart(dto.getDate(),dto.getClientTimeZone());
            forbiddenSlots.add(new TimeRange(dayStart,dayStart.plus(Duration.ofHours(7))));
            forbiddenSlots.add(new TimeRange(dayStart.plus(Duration.ofHours(12)),dayStart.plus(Duration.ofHours(14))));
            // 2. 计算智能规划结果
            ScheduleResult scheduleResult = smartScheduleDailyPlan(dayStart,adHocEvents,
                    habitualEvents,forbiddenSlots );

            return Response.success(scheduleResult, "规划成功");

        } catch (Exception e) {
            log.error("智能规划失败", e);
            return Response.error(ResponseCode.UN_ERROR, "智能规划失败");
        }
    }

    // ===================================================================
    //                          调度算法核心部分，todo 多种情况多种处理
    // ===================================================================
    //1.时间段分段，用对应记录是否使用
    //2.事件优先级：紧急事件，优先级，固定时间
    //3.这个是紧急事件固定时间，在空余的时间段，插入日常事件（可分多段），尽可能每个事件之间都有5分钟或以上的时间间隔
    //4.在空余的时间段，优先选用事件之间的空闲，然后是事件最开始时间段与最后时间段的外面，但是不能在禁止时间段安排
    //5.紧急事件冲突不管，依旧放入结果，日常事件没有时间段可以放入，则记录未完成放入的内容,放入redis做对应的补偿
    //6.紧急事件冲突不管，依旧放入结果，返回结果有对应的冲突事件集的事件id
    private ScheduleResult smartScheduleDailyPlan(
            Instant dayStart,
            List<AdHocEventEntity> adHocEvents,
            List<HabitualEventEntity> habitualEvents,
            List<TimeRange> forbiddenSlots) { // 新增禁止时间段参数

        try {
            // 参数验证
            validateInputParameters(dayStart, adHocEvents, habitualEvents);

            List<PlannedTask> results = new ArrayList<>();
            List<Long> conflictIds = new ArrayList<>();
            List<UnscheduledTask> unscheduledTasks = new ArrayList<>();

            // 1. 构建统一任务列表
            List<SchedulerTask> tasks = buildSchedulerTasks(adHocEvents, habitualEvents);

            // 2. 排序：突发事件优先，然后按优先级排序
            tasks.sort(Comparator.comparing(SchedulerTask::isAdHoc).reversed()
                    .thenComparing(SchedulerTask::getPriority));

            // 3. 当天时间段初始化
            List<TimeSlot> timeSlots = initializeTimeSlots(dayStart);

            // 3.1 标记禁止时间段
            if (forbiddenSlots != null && !forbiddenSlots.isEmpty()) {
                for (TimeRange forbidden : forbiddenSlots) {
                    markSlotsUsed(timeSlots, forbidden.getStart(), forbidden.getEnd());
                }
            }

            // 4. 优化调度：按照时间段优先级调度
            scheduleTasksOptimized(tasks, timeSlots, results, conflictIds, unscheduledTasks);

            // 5. Redis补偿机制
            if (!unscheduledTasks.isEmpty()) {
                compensateToRedis(unscheduledTasks);
            }

            return ScheduleResult.builder()
                    .plannedTasks(results)
                    .conflictTaskIds(conflictIds)
                    .unscheduledTasks(unscheduledTasks)
                    .build();

        } catch (Exception e) {
            log.error("智能日程调度失败", e);
            throw new ScheduleException("日程调度执行失败", e);
        }
    }

    /**
     * 优化调度策略：优先使用事件间的空闲时间
     */
    private void scheduleTasksOptimized(List<SchedulerTask> tasks, List<TimeSlot> timeSlots,
                                        List<PlannedTask> results, List<Long> conflictIds,
                                        List<UnscheduledTask> unscheduledTasks) {

        for (SchedulerTask task : tasks) {
            if (task.getFixedStart() != null && task.getFixedEnd() != null) {
                // 固定时间任务（突发事件）
                if (isConflict(results, task.getFixedStart(), task.getFixedEnd())) {
                    conflictIds.add(task.getId());
                    log.warn("突发事件冲突: {}", task.getTitle());
                }
                results.add(toPlannedTask(task, task.getFixedStart(), task.getFixedEnd()));
                markSlotsUsed(timeSlots, task.getFixedStart(), task.getFixedEnd());
                continue;
            }

            // 普通任务（日常事件），按优化策略调度
            scheduleHabitualTask(task, timeSlots, results, unscheduledTasks);
        }
    }

    /**
     * 调度日常事件的优化策略 todo 偏爱时间段，重复模式
     */
    private void scheduleHabitualTask(SchedulerTask task, List<TimeSlot> timeSlots,
                                      List<PlannedTask> results, List<UnscheduledTask> unscheduledTasks) {

        BigDecimal remainingHours = task.getDurationHours();
        List<PlannedTask> taskParts = new ArrayList<>();

        // 按时间段优先级排序：优先使用事件间的空闲   todo 这里是所有先用短的，在往前排
        List<TimeSlot> sortedSlots = timeSlots.stream()
                .filter(slot -> !slot.isUsed())
                .sorted(this::compareTimeSlots)
                .toList();

        for (TimeSlot slot : sortedSlots) {
            if (remainingHours.compareTo(BigDecimal.ZERO) <= 0) break;

            Duration slotDuration = Duration.between(slot.getStart(), slot.getEnd());
            double slotHours = slotDuration.toMinutes() / 60.0;

            if (slotHours < MIN_TASK_DURATION_HOURS) continue; // 最小任务时间过滤

            double useHours = Math.min(remainingHours.doubleValue(), slotHours);
            Instant taskStart = slot.getStart();
            Instant taskEnd = taskStart.plus(Duration.ofMinutes((long) (useHours * 60)));

            PlannedTask partTask = toPlannedTask(task, taskStart, taskEnd);
            taskParts.add(partTask);

            // 保留5分钟间隔
            Instant endWithBuffer = taskEnd.plus(Duration.ofMinutes(BUFFER_MINUTES));
            markSlotsUsed(timeSlots, taskStart, endWithBuffer);

            remainingHours = remainingHours.subtract(BigDecimal.valueOf(useHours));
        }

        if (!taskParts.isEmpty()) {
            results.addAll(taskParts);
        }

        if (remainingHours.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("日常事件 {} 时间不足，剩余 {} 小时未排", task.getTitle(), remainingHours);
            unscheduledTasks.add(UnscheduledTask.builder()
                    .taskId(task.getId())
                    .title(task.getTitle())
                    .remainingHours(remainingHours)
                    .originalDuration(task.getDurationHours())
                    .scheduledHours(task.getDurationHours().subtract(remainingHours))
                    .build());
        }
    }

    /**
     * 时间段优先级比较：优先选择较小的时间段（事件间空闲） todo 优化
     */
    private int compareTimeSlots(TimeSlot slot1, TimeSlot slot2) {
        Duration duration1 = Duration.between(slot1.getStart(), slot1.getEnd());
        Duration duration2 = Duration.between(slot2.getStart(), slot2.getEnd());

        // 优先选择较短的时间段（更可能是事件间的空闲）
        int durationCompare = duration1.compareTo(duration2);
        if (durationCompare != 0) {
            return durationCompare;
        }

        // 时间段相同时，优先选择较早的
        return slot1.getStart().compareTo(slot2.getStart());
    }

    /**
     * Redis补偿机制 todo 使用redis服务
     */
    private void compensateToRedis(List<UnscheduledTask> unscheduledTasks) {
        try {
//            String redisKey = generateRedisKey();
//
//            // 构建Redis数据
//            Map<String, Object> compensationData = unscheduledTasks.stream()
//                    .collect(Collectors.toMap(
//                            task -> "task_" + task.getTaskId(),
//                            task -> Map.of(
//                                    "title", task.getTitle(),
//                                    "remainingHours", task.getRemainingHours(),
//                                    "originalDuration", task.getOriginalDuration(),
//                                    "scheduledHours", task.getScheduledHours(),
//                                    "timestamp", System.currentTimeMillis()
//                            )
//                    ));
//
//            // 存储到Redis
////            redisTemplate.opsForHash().putAll(redisKey, compensationData);
////            redisTemplate.expire(redisKey, Duration.ofDays(7)); // 7天过期
//
//            log.info("未完成任务已补偿到Redis: {} 个任务", unscheduledTasks.size());

        } catch (Exception e) {
            log.error("Redis补偿失败", e);
            // Redis失败不应该影响主要逻辑，可以记录但不抛出异常
        }
    }

    /**
     * 参数验证
     */
    private void validateInputParameters(Instant date, List<AdHocEventEntity> adHocEvents,
                                         List<HabitualEventEntity> habitualEvents) {
        if (date == null) {
            throw new IllegalArgumentException("日期参数不能为空");
        }

        if (adHocEvents == null) {
            adHocEvents = new ArrayList<>();
        }

        if (habitualEvents == null) {
            habitualEvents = new ArrayList<>();
        }

        // 验证事件数据完整性
        adHocEvents.forEach(event -> {
            if (event.getId() == null || event.getTitle() == null) {
                throw new IllegalArgumentException("突发事件数据不完整");
            }
        });

        habitualEvents.forEach(event -> {
            if (event.getId() == null || event.getTitle() == null ||
                    event.getEstimatedTime() == null || event.getEstimatedTime().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("日常事件数据不完整或时间无效");
            }
        });
    }

    /**
     * 构建调度任务列表
     */
    private List<SchedulerTask> buildSchedulerTasks(List<AdHocEventEntity> adHocEvents,
                                                    List<HabitualEventEntity> habitualEvents) {
        List<SchedulerTask> tasks = new ArrayList<>();

        adHocEvents.forEach(e -> tasks.add(SchedulerTask.builder()
                .id(e.getId())
                .title(e.getTitle())
                .fixedStart(e.getPlannedStartTime())
                .fixedEnd(e.getPlannedEndTime())
                .isAdHoc(true)
                .priority(e.getQuadrant())
                .build()
        ));

        habitualEvents.forEach(e -> tasks.add(SchedulerTask.builder()
                .id(e.getId())
                .title(e.getTitle())
                .durationHours(e.getEstimatedTime())
                //todo 偏爱时间段
                //todo 周期
                .isHabitual(true)
                .priority(e.getQuadrant())
                .build()
        ));

        return tasks;
    }

    /**
     * 初始化时间段
     */
    private List<TimeSlot> initializeTimeSlots(Instant dayStart) {
        Instant dayEnd = dayStart.plus(Duration.ofHours(24));

        List<TimeSlot> timeSlots = new ArrayList<>();
        timeSlots.add(TimeSlot.builder()
                .start(dayStart)
                .end(dayEnd)
                .used(false)
                .build());

        return timeSlots;
    }

    private PlannedTask toPlannedTask(SchedulerTask task, Instant start, Instant end) {
        return PlannedTask.builder()
                .id(task.getId())
                .title(task.getTitle())
                .startTime(start)
                .endTime(end)
                .type(task.isAdHoc() ? "ad_hoc" : "habitual")
                .priority(task.getPriority())
                .build();
    }

    private boolean isConflict(List<PlannedTask> results, Instant start, Instant end) {
        return results.stream()
                .anyMatch(t -> !(t.getEndTime().isBefore(start) || t.getStartTime().isAfter(end)
                        || t.getEndTime().equals(start) || t.getStartTime().equals(end)));
    }

    /**
     * 优化的时间段标记方法
     */
    private void markSlotsUsed(List<TimeSlot> slots, Instant start, Instant end) {
        List<TimeSlot> newSlots = new ArrayList<>();

        for (TimeSlot slot : slots) {
            if (slot.isUsed()) {
                newSlots.add(slot);
                continue;
            }

            // 情况1: 完全不重叠
            if (isNonOverlapping(slot, start, end)) {
                newSlots.add(slot);
                continue;
            }

            // 情况2: 部分或完全重叠，需要分割
            splitAndAddSlot(newSlots, slot, start, end);
        }

        slots.clear();
        slots.addAll(newSlots);
    }

    /**
     * 检查是否不重叠
     */
    private boolean isNonOverlapping(TimeSlot slot, Instant start, Instant end) {
        return slot.getEnd().isBefore(start) || slot.getEnd().equals(start)
                || slot.getStart().isAfter(end) || slot.getStart().equals(end);
    }

    /**
     * 分割时间段并添加到列表，todo 优化，怎么减少碎片，优化时间
     */
    private void splitAndAddSlot(List<TimeSlot> newSlots, TimeSlot slot, Instant start, Instant end) {
        Instant slotStart = slot.getStart();
        Instant slotEnd = slot.getEnd();

        // 前段空闲
        if (slotStart.isBefore(start)) {
            newSlots.add(createTimeSlot(slotStart, start, false));
        }

        // 中间已使用段
        Instant usedStart = slotStart.isAfter(start) ? slotStart : start;
        Instant usedEnd = slotEnd.isBefore(end) ? slotEnd : end;
        newSlots.add(createTimeSlot(usedStart, usedEnd, true));

        // 后段空闲
        if (slotEnd.isAfter(end)) {
            newSlots.add(createTimeSlot(end, slotEnd, false));
        }
    }

    /**
     * 创建时间段
     */
    private TimeSlot createTimeSlot(Instant start, Instant end, boolean used) {
        // 添加有效性检查
        if (start.isAfter(end) || start.equals(end)) {
            return null;
        }
        return TimeSlot.builder()
                .start(start)
                .end(end)
                .used(used)
                .build();
    }

    // 常量定义
    private static final int BUFFER_MINUTES = 5;
    private static final double MIN_TASK_DURATION_HOURS = 0.25; // 最小任务时间15分钟

    @Builder
    @Data
    static class SchedulerTask {
        private Long id;
        private String title;
        private BigDecimal durationHours; // 小时
        private Instant fixedStart;   // 突发事件固定时间
        private Instant fixedEnd;
        private boolean isAdHoc;
        private boolean isHabitual;
        private Integer priority;
    }

    @Builder
    @Data
    static class TimeSlot {
        private Instant start;
        private Instant end;
        private boolean used;
    }

    @Builder
    @Data
    static class TimeRange {
        private Instant start;
        private Instant end;
    }

    @Builder
    @Data
    static class UnscheduledTask {
        private Long taskId;
        private String title;
        private BigDecimal remainingHours;
        private BigDecimal originalDuration;
        private BigDecimal scheduledHours;
    }

    @Builder
    @Data
    static class ScheduleResult {
        private List<PlannedTask> plannedTasks;
        private List<Long> conflictTaskIds;
        private List<UnscheduledTask> unscheduledTasks;
    }

    @Data
    @Builder
    public static class PlannedTask {
        private Long id;
        private String title;
        private Instant startTime;
        private Instant endTime;
        private String type;
        private Integer priority;
    }

    /**
     * 自定义异常类
     */
    public static class ScheduleException extends RuntimeException {
        public ScheduleException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
