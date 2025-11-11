package org.cxk.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.cxk.domain.model.entity.AdHocEventEntity;
import org.cxk.domain.repository.IAdHocEventRepository;
import org.cxk.infrastructure.adapter.dao.IAdHocEventDao;
import org.cxk.infrastructure.adapter.dao.po.AdHocEvent;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author KJH
 * @description 突发事件仓储实现（领域模型 ↔ 持久化模型 映射层）
 * @create 2025/10/26 09:33
 */
@Repository
@RequiredArgsConstructor
public class AdHocEventRepository implements IAdHocEventRepository {

    private final IAdHocEventDao adHocEventDao;
    private final RedissonClient redissonClient;

    /**
     * 查询用户的突发事件列表
     */
    @Override
    public List<AdHocEventEntity> findByUserId(Long userId) {
        return adHocEventDao.selectByUserId(userId)
                .stream()
                .map(this::toEntity) // ✅ 复用统一映射方法
                .collect(Collectors.toList());
    }

    /**
     * 更新事件
     */
    @Override
    public void update(AdHocEvent event) {
//        adHocEventDao.updateById(event);
        if (event == null || event.getId() == null) return;

        adHocEventDao.update(
                null,
                new LambdaUpdateWrapper<AdHocEvent>()
                        .eq(AdHocEvent::getId, event.getId())
                        .set(event.getUserId() != null, AdHocEvent::getUserId, event.getUserId())
                        .set(event.getTitle() != null, AdHocEvent::getTitle, event.getTitle())
                        .set(event.getDescription() != null, AdHocEvent::getDescription, event.getDescription())
                        .set(event.getQuadrant() != null, AdHocEvent::getQuadrant, event.getQuadrant())
                        .set(event.getPlannedStartTime() != null, AdHocEvent::getPlannedStartTime, event.getPlannedStartTime())
                        .set(event.getPlannedEndTime() != null, AdHocEvent::getPlannedEndTime, event.getPlannedEndTime())
                        .set(event.getDeadline() != null, AdHocEvent::getDeadline, event.getDeadline())
                        .set(event.getStatus() != null, AdHocEvent::getStatus, event.getStatus())
                        .set(event.getMeasurementUnit() != null, AdHocEvent::getMeasurementUnit, event.getMeasurementUnit())
                        .set(event.getTargetQuantity() != null, AdHocEvent::getTargetQuantity, event.getTargetQuantity())
                        .set(event.getCompletedQuantity() != null, AdHocEvent::getCompletedQuantity, event.getCompletedQuantity())
                        .set(event.getIsDeleted() != null, AdHocEvent::getIsDeleted, event.getIsDeleted())
        );
    }


    /**
     * 根据ID查询事件
     */
    @Override
    public AdHocEvent findById(Long eventId) {
        return adHocEventDao.selectById(eventId);
    }

    /**
     * 保存事件
     */
    @Override
    public void save(AdHocEvent event) {
        adHocEventDao.insert(event);
    }

    /**
     * 删除事件
     */
    @Override
    public void delete(Long userId, Long eventId) {
        adHocEventDao.deleteByUserIdAndId(userId, eventId);
    }

    /**
     * PO → Entity 映射
     */
    private AdHocEventEntity toEntity(AdHocEvent po) {
        if (po == null) return null;
        AdHocEventEntity entity = new AdHocEventEntity();
        entity.setId(po.getId());
        entity.setUserId(po.getUserId());
        entity.setTitle(po.getTitle());
        entity.setDescription(po.getDescription());
        entity.setQuadrant(po.getQuadrant());
        entity.setPlannedStartTime(po.getPlannedStartTime());
        entity.setPlannedEndTime(po.getPlannedEndTime());
        entity.setDeadline(po.getDeadline());
        entity.setStatus(po.getStatus());
        entity.setMeasurementUnit(po.getMeasurementUnit());
        entity.setTargetQuantity(po.getTargetQuantity());
        entity.setCompletedQuantity(po.getCompletedQuantity());
        return entity;
    }

}
