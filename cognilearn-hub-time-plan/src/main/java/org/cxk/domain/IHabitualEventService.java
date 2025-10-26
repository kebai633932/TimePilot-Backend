package org.cxk.domain;

import org.cxk.domain.entity.HabitualEvent;
import org.cxk.types.dto.HabitualEventQueryDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author KJH
 * @description 日常事件（习惯性事件）领域服务接口
 * @create 2025/4/25 0:56
 */
public interface IHabitualEventService {

    /**
     * 创建日常事件
     */
    HabitualEvent createHabitualEvent(Long userId, String title, String description,
                                      Integer quadrant, Integer energyLevel,
                                      BigDecimal durationHours, Integer priority,
                                      LocalDate startDate, LocalDate endDate,
                                      String preferredTimeSlots, String repeatPattern,
                                      List<Long> tagIds);

    /**
     * 更新日常事件
     */
    HabitualEvent updateHabitualEvent(Long userId, Long eventId, String title,
                                      String description, Integer quadrant,
                                      Integer energyLevel, BigDecimal durationHours,
                                      Integer priority, LocalDate startDate,
                                      LocalDate endDate, String preferredTimeSlots,
                                      String repeatPattern, Integer status,
                                      List<Long> tagIds);

    /**
     * 删除日常事件（逻辑删除）
     */
    void deleteHabitualEvent(Long userId, Long eventId);

    /**
     * 根据ID查询日常事件
     */
    HabitualEvent getHabitualEventById(Long userId, Long eventId);

    /**
     * 查询日常事件列表
     */
    List<HabitualEvent> listHabitualEvents(Long userId, HabitualEventQueryDTO queryDTO);

    /**
     * 更新完成率
     */
    void updateCompletionRate(Long userId, Long eventId, BigDecimal completionRate);

    /**
     * 启用/停用日常事件
     */
    void toggleHabitualEventStatus(Long userId, Long eventId, Integer status);

    /**
     * 验证事件所有权
     */
    void validateEventOwnership(Long userId, Long eventId);

    /**
     * 获取用户日常事件统计
     */
    Map<String, Object> getUserHabitualEventStats(Long userId);

    /**
     * 根据日期查询有效的日常事件
     */
    List<HabitualEvent> listValidHabitualEventsByDate(Long userId, LocalDate date);

    /**
     * 检查时间冲突
     */
    boolean hasTimeConflict(Long userId, LocalDate date, String timeSlots, Long excludeEventId);
}