package org.cxk.infrastructure.adapter.repository;

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
 * @description 突发事件仓储实现
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
        List<AdHocEvent> list = adHocEventDao.selectByUserId(userId);
        return list.stream().map(e -> {
            AdHocEventEntity entity = new AdHocEventEntity();
            entity.setId(e.getId());
            entity.setUserId(e.getUserId());
            entity.setTitle(e.getTitle());
            entity.setQuadrant(e.getQuadrant());
            entity.setPlannedStartTime(e.getPlannedStartTime());
            entity.setPlannedEndTime(e.getPlannedEndTime());
            return entity;
        }).collect(Collectors.toList());
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
     * 更新事件
     */
    @Override
    public void update(AdHocEvent event) {
        adHocEventDao.updateById(event);
    }

    /**
     * 删除事件
     */
    @Override
    public void delete(Long userId, Long eventId) {
        adHocEventDao.deleteByUserIdAndId(userId, eventId);
    }
}
