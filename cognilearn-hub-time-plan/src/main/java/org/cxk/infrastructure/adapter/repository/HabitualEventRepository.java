package org.cxk.infrastructure.adapter.repository;

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
 * @description 日常事件仓储实现
 * @create 2025/10/26 09:32
 */
@Repository
@RequiredArgsConstructor
public class HabitualEventRepository implements IHabitualEventRepository {

    private final IHabitualEventDao habitualEventDao;

    private final RedissonClient redissonClient;

    @Override
    public List<HabitualEventEntity> findByUserId(Long userId) {
        List<HabitualEvent> list = habitualEventDao.selectByUserId(userId);
        return list.stream().map(e -> {
            HabitualEventEntity entity = new HabitualEventEntity();
            entity.setId(e.getId());
            entity.setUserId(e.getUserId());
            entity.setTitle(e.getTitle());
            entity.setQuadrant(e.getQuadrant());
            entity.setStartDate(e.getStartDate());
            entity.setEndDate(e.getEndDate());
            return entity;
        }).collect(Collectors.toList());
    }

    @Override
    public HabitualEvent findById(Long eventId) {
        return habitualEventDao.selectById(eventId);
    }

    @Override
    public void save(HabitualEvent event) {
        habitualEventDao.insert(event);
    }

    @Override
    public void update(HabitualEvent event) {
        habitualEventDao.updateById(event);
    }

    @Override
    public void delete(Long eventId) {
        habitualEventDao.deleteById(eventId);
    }
}
