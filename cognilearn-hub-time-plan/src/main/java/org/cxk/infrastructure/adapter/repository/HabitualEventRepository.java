package org.cxk.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.cxk.domain.model.entity.HabitualEventEntity;
import org.cxk.domain.repository.IHabitualEventRepository;
import org.cxk.infrastructure.adapter.dao.IHabitualEventDao;
import org.cxk.infrastructure.adapter.dao.po.HabitualEvent;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author KJH
 * @description 日常事件仓储实现（领域模型 ↔ 持久化模型映射）
 * @create 2025/10/26
 */
@Repository
@RequiredArgsConstructor
public class HabitualEventRepository implements IHabitualEventRepository {

    private final IHabitualEventDao habitualEventDao;
    private final RedissonClient redissonClient;

    /**
     * 查询用户的日常事件列表
     */
    @Override
    public List<HabitualEventEntity> findByUserId(Long userId) {
        return habitualEventDao.selectByUserId(userId)
                .stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查询事件
     */
    @Override
    public HabitualEvent findById(Long eventId) {
        return habitualEventDao.selectById(eventId);
    }

    /**
     * 保存事件
     */
    @Override
    public void save(HabitualEvent event) {
        habitualEventDao.insert(event);
    }

    /**
     * 更新事件（空字段不会覆盖数据库已有值）
     */
    @Override
    public void update(HabitualEvent event) {
        if (event == null || event.getId() == null) return;

        habitualEventDao.update(
                null,
                new LambdaUpdateWrapper<HabitualEvent>()
                        .eq(HabitualEvent::getId, event.getId())
                        .set(event.getUserId() != null, HabitualEvent::getUserId, event.getUserId())
                        .set(event.getTitle() != null, HabitualEvent::getTitle, event.getTitle())
                        .set(event.getDescription() != null, HabitualEvent::getDescription, event.getDescription())
                        .set(event.getQuadrant() != null, HabitualEvent::getQuadrant, event.getQuadrant())
                        .set(event.getEstimatedTime() != null, HabitualEvent::getEstimatedTime, event.getEstimatedTime())
                        .set(event.getPreferredTimeSlots() != null, HabitualEvent::getPreferredTimeSlots, event.getPreferredTimeSlots())
                        .set(event.getRepeatPattern() != null, HabitualEvent::getRepeatPattern, event.getRepeatPattern())
                        .set(event.getRepeatInterval() != null, HabitualEvent::getRepeatInterval, event.getRepeatInterval())
                        .set(event.getCompletionRate() != null, HabitualEvent::getCompletionRate, event.getCompletionRate())
                        .set(event.getMeasurementUnit() != null, HabitualEvent::getMeasurementUnit, event.getMeasurementUnit())
                        .set(event.getTargetQuantity() != null, HabitualEvent::getTargetQuantity, event.getTargetQuantity())
                        .set(event.getCompletedQuantity() != null, HabitualEvent::getCompletedQuantity, event.getCompletedQuantity())
                        .set(event.getIsDeleted() != null, HabitualEvent::getIsDeleted, event.getIsDeleted())
        );
    }


    /**
     * 删除事件
     */
    @Override
    public void delete(Long eventId) {
        habitualEventDao.deleteById(eventId);
    }

    /**
     * PO → Entity 映射
     */
    private HabitualEventEntity toEntity(HabitualEvent po) {
        if (po == null) return null;

        HabitualEventEntity entity = new HabitualEventEntity();
        entity.setId(po.getId());
        entity.setUserId(po.getUserId());
        entity.setTitle(po.getTitle());
        entity.setDescription(po.getDescription());
        entity.setQuadrant(po.getQuadrant());
        entity.setEstimatedTime(po.getEstimatedTime());
        entity.setPreferredTimeSlots(po.getPreferredTimeSlots());
        entity.setRepeatPattern(po.getRepeatPattern());
        entity.setRepeatInterval(po.getRepeatInterval());
        entity.setCompletionRate(po.getCompletionRate());
        entity.setMeasurementUnit(po.getMeasurementUnit());
        entity.setTargetQuantity(po.getTargetQuantity());
        entity.setCompletedQuantity(po.getCompletedQuantity());

        return entity;
    }
}
