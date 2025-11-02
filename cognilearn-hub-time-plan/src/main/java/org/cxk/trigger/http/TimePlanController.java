package org.cxk.trigger.http;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.cxk.api.dto.SmartDailyPlanGenerateDTO;
import org.cxk.domain.IAdHocEventService;
import org.cxk.domain.IHabitualEventService;
import org.cxk.domain.model.entity.AdHocEventEntity;
import org.cxk.domain.model.entity.HabitualEventEntity;
import org.cxk.infrastructure.adapter.dao.po.AdHocEvent;
import org.cxk.infrastructure.adapter.dao.po.HabitualEvent;
import org.cxk.util.AuthenticationUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author KJH
 * @description 时间安排管理（智能规划）
 * @create 2025/10/26
 */
@Slf4j
@RestController
@RequestMapping("/api/time-plan")
@AllArgsConstructor
public class TimePlanController {

    private final IAdHocEventService adHocEventService;
    private final IHabitualEventService habitualEventService;

    /**
     * 智能规划当日时间安排
     */
    @PostMapping("/smart-daily-plan")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> generateSmartDailyPlan(@Valid @RequestBody SmartDailyPlanGenerateDTO dto) {
        Long userId = AuthenticationUtil.getCurrentUserId();

        try {
            // 获取当天突发事件
            List<AdHocEventEntity> adHocEvents = adHocEventService.getTodayEvents(userId,dto.getDate());
            // 获取当天习惯事件
            List<HabitualEventEntity> habitualEvents = habitualEventService.getTodayEvents(userId,dto.getDate());

            // 智能调度突发事件 习惯事件
            List<PlannedEvent> plannedAdHocEvents = scheduleHabitualEvents(adHocEvents,habitualEvents);

            // TODO: 保存规划结果，可调用 dailyPlanService.saveDailyPlan(userId, finalPlan)

            log.info("[智能规划成功] userId={}, 生成事件数={}", userId, plannedAdHocEvents.size());
            return ResponseEntity.ok(plannedAdHocEvents);

        } catch (ScheduleConflictException e) {
            log.warn("[智能规划失败] userId={}, 冲突: {}", userId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("智能规划失败：存在无法调解的时间冲突 -> " + e.getMessage());

        } catch (Exception e) {
            log.error("[智能规划异常] userId={}, err={}", userId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("智能规划发生异常：" + e.getMessage());
        }
    }

    // ========================== 调度算法部分 =============================== //

    /**
     * 调度习惯事件
     */
    private List<PlannedEvent> scheduleHabitualEvents(
            List<AdHocEventEntity> adHocEvents, List<HabitualEventEntity> habitualEvents) {
        List<PlannedEvent> result = new ArrayList<>();

        // 1️⃣ 先加入所有突发事件（假设这些已经确定）
        for (AdHocEventEntity e : adHocEvents) {
            result.add(new PlannedEvent(
                    e.getTitle(),
                    e.getPlannedStartTime(),
                    e.getPlannedEndTime(),
                    "adhoc"
            ));
        }

        // 2️⃣ 对习惯事件按 quadrant 排序：优先安排重要紧急 → 紧急不重要 → 重要不紧急 → 不重要不紧急
        habitualEvents.sort(Comparator.comparingInt(HabitualEventEntity::getQuadrant).reversed());

        // 3️⃣ 遍历安排
        for (HabitualEventEntity habit : habitualEvents) {
            Instant start = habit.getStartDate();
            Instant end = habit.getEndDate();
            int quadrant = habit.getQuadrant();
            boolean conflict = false;

            // 检查与已有事件是否冲突
            for (PlannedEvent existing : result) {
                if (timeOverlap(start, end, existing.getStartTime(), existing.getEndTime())) {
                    conflict = true;
                    switch (quadrant) {
                        case 1 -> { // 重要紧急
                            throw new ScheduleConflictException
                                    ("【重要紧急】事件 [" +habit.getTitle() + "] 与 [" + existing.getTitle() + "] 冲突，无法自动调解");
                        }
                        case 3, 4 -> { // 不重要，尝试前后调整
                            Instant beforeSlotEnd = existing.getStartTime().minus(java.time.Duration.ofMinutes(5));
                            Instant afterSlotStart = existing.getEndTime().plus(java.time.Duration.ofMinutes(5));

                            long availableBefore = DurationMinutes(beforeSlotEnd, start);
                            long availableAfter = DurationMinutes(end, afterSlotStart);

                            if (availableBefore >= 15) {
                                // 可以提前安排
                                end = beforeSlotEnd;
                                conflict = false;
                            } else if (availableAfter >= 15) {
                                // 可以延后安排
                                start = afterSlotStart;
                                conflict = false;
                            } else {
                                throw new ScheduleConflictException("【不重要】事件 [" + habit.getTitle() + "] 没有足够时间可调整");
                            }
                        }

                        case 2 -> { // 重要不紧急：缩短时长（保留前半）
                            long totalDuration = DurationMinutes(start, end);
                            Instant newEnd = start.plus(java.time.Duration.ofMinutes(Math.max(15, totalDuration / 2)));

                            if (!timeOverlap(start, newEnd, existing.getStartTime(), existing.getEndTime())) {
                                end = newEnd;
                                conflict = false;
                            } else {
                                throw new ScheduleConflictException("【重要不紧急】事件 [" + habit.getTitle() + "] 无法缩短以避免冲突");
                            }
                        }

                    }
                    if (conflict) break;
                }
            }

            // 冲突调解完成或无冲突 -> 加入结果
            result.add(new PlannedEvent(habit.getTitle(), start, end, "habitual"));
        }

        // 排序输出
        result.sort(Comparator.comparing(PlannedEvent::getStartTime));
        return result;
    }

    /**
     * 判断两个时间段是否重叠
     */
    private boolean timeOverlap(Instant start1, Instant end1, Instant start2, Instant end2) {
        // start1 < end2 && end1 > start2 表示有交集
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    /**
     * 计算两个时间点之间的分钟数
     */
    private long DurationMinutes(Instant start, Instant end) {
        return java.time.Duration.between(start, end).toMinutes();
    }


    @Data
    public static class PlannedEvent {
        private String title;
        private Instant startTime;
        private Instant endTime;
        private String type; // adhoc / habitual

        public PlannedEvent(String title, Instant startTime, Instant endTime, String type) {
            this.title = title;
            this.startTime = startTime;
            this.endTime = endTime;
            this.type = type;
        }
        public PlannedEvent() {
            this.title = null;
            this.startTime = null;
            this.endTime = null;
            this.type = null;
        }
    }

    public static class ScheduleConflictException extends RuntimeException {
        public ScheduleConflictException(String message) {
            super(message);
        }
    }
}
