package org.cxk.domain.repository;

import org.cxk.domain.model.entity.AdHocEventEntity;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author KJH
 * @description 突发事件仓储接口
 * @create 2025/10/26 09:34
 */
public interface IAdHocEventRepository {

    /**
     * 根据事件ID查询
     */
    Optional<AdHocEventEntity> findByEventId(Long eventId);

    /**
     * 保存突发事件
     */
    void save(AdHocEventEntity eventEntity);

    /**
     * 删除突发事件
     */
    void delete(AdHocEventEntity eventEntity);

    /**
     * 查询用户的所有突发事件
     */
    List<AdHocEventEntity> findByUserId(Long userId);

    /**
     * 根据事件ID和用户ID查询
     */
    Optional<AdHocEventEntity> findByEventIdAndUserId(Long eventId, Long userId);

    /**
     * 根据状态查询用户的突发事件
     */
    List<AdHocEventEntity> findByUserIdAndStatus(Long userId, Integer status);

    /**
     * 查询今日待办的突发事件
     */
    List<AdHocEventEntity> findTodayEvents(Long userId);

    /**
     * 查询逾期未完成的突发事件
     */
    List<AdHocEventEntity> findOverdueEvents(Long userId);

    /**
     * 根据截止时间范围查询
     */
    List<AdHocEventEntity> findByDeadlineRange(Long userId, Date startTime, Date endTime);

    /**
     * 更新事件状态
     */
    void updateStatus(Long eventId, Long userId, Integer status);

    /**
     * 记录实际时间
     */
    void recordActualTime(Long eventId, Long userId, Date actualStartTime, Date actualEndTime, Double actualHours);

    /**
     * 检查时间冲突
     */
    boolean hasTimeConflict(Long userId, Date startTime, Date endTime, Long excludeEventId);

    /**
     * 获取用户突发事件统计
     */
    Object getUserAdHocEventStats(Long userId);

    /**
     * 关联习惯性事件
     */
    void linkToHabitualEvent(Long adHocEventId, Long habitualEventId);
}