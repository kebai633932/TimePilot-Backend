package org.cxk.domain.repository;

import org.cxk.domain.model.entity.HabitualEventEntity;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author KJH
 * @description 习惯性事件仓储接口
 * @create 2025/10/26 09:34
 */
public interface IHabitualEventRepository {

    /**
     * 根据事件ID查询
     */
    Optional<HabitualEventEntity> findByEventId(Long eventId);

    /**
     * 保存习惯性事件
     */
    void save(HabitualEventEntity eventEntity);

    /**
     * 删除习惯性事件
     */
    void delete(HabitualEventEntity eventEntity);

    /**
     * 查询用户的所有习惯性事件
     */
    List<HabitualEventEntity> findByUserId(Long userId);

    /**
     * 根据事件ID和用户ID查询
     */
    Optional<HabitualEventEntity> findByEventIdAndUserId(Long eventId, Long userId);

    /**
     * 根据状态查询用户的习惯性事件
     */
    List<HabitualEventEntity> findByUserIdAndStatus(Long userId, Integer status);

    /**
     * 根据四象限查询用户的习惯性事件
     */
    List<HabitualEventEntity> findByUserIdAndQuadrant(Long userId, Integer quadrant);

    /**
     * 查询指定日期的有效习惯性事件
     */
    List<HabitualEventEntity> findValidEventsByDate(Long userId, Date date);

    /**
     * 更新完成率
     */
    void updateCompletionRate(Long eventId, Long userId, Double completionRate);

    /**
     * 更新事件状态
     */
    void updateStatus(Long eventId, Long userId, Integer status);

    /**
     * 检查时间冲突
     */
    boolean hasTimeConflict(Long userId, Date date, String timeSlots, Long excludeEventId);

    /**
     * 获取用户习惯性事件统计
     */
    Object getUserHabitualEventStats(Long userId);
}