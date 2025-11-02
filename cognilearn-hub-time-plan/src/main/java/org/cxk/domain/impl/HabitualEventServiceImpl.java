package org.cxk.domain.impl;

import jakarta.annotation.Resource;
import org.cxk.api.dto.HabitualEventCreateDTO;
import org.cxk.api.dto.HabitualEventUpdateDTO;
import org.cxk.api.response.HabitualEventVO;
import org.cxk.domain.IHabitualEventService;
import org.cxk.domain.model.entity.HabitualEventEntity;
import org.cxk.domain.repository.IHabitualEventRepository;
import org.cxk.infrastructure.adapter.dao.po.HabitualEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author KJH
 * @description 习惯性事件领域服务实现
 */
@Service
public class HabitualEventServiceImpl implements IHabitualEventService {

    @Resource
    private IHabitualEventRepository habitualEventRepository;

    /**
     * 删除习惯性事件
     */
    @Override
    public void deleteHabitualEvent(Long userId, Long eventId) {
        HabitualEvent event = habitualEventRepository.findById(eventId);
        Assert.notNull(event, "事件不存在");
        Assert.isTrue(event.getUserId().equals(userId), "无权删除他人事件");
        habitualEventRepository.delete(eventId);
    }

    /**
     * 创建习惯性事件
     */
    @Override
    public Long createHabitualEvent(Long userId, HabitualEventCreateDTO dto) {
        HabitualEvent event = new HabitualEvent();
        event.setUserId(userId);
        event.setTitle(dto.getTitle());
        event.setQuadrant(dto.getQuadrant());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        habitualEventRepository.save(event);
        return event.getId();
    }

    /**
     * 更新习惯性事件
     */
    @Override
    public void updateHabitualEvent(Long userId, HabitualEventUpdateDTO dto) {
        HabitualEvent event = habitualEventRepository.findById(dto.getEventId());
        Assert.notNull(event, "事件不存在");
        Assert.isTrue(event.getUserId().equals(userId), "无权修改他人事件");

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setQuadrant(dto.getQuadrant());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());

        habitualEventRepository.update(event);
    }

    /**
     * 查询用户习惯性事件列表
     */
    @Override
    public List<HabitualEventVO> listUserHabitualEvents(Long userId) {
        List<HabitualEventEntity> list = habitualEventRepository.findByUserId(userId);
        return list.stream().map(e -> {
            HabitualEventVO vo = new HabitualEventVO();
            vo.setEventId(e.getId());
            vo.setTitle(e.getTitle());
            vo.setQuadrant(e.getQuadrant());
            vo.setStartDate(e.getStartDate());
            vo.setEndDate(e.getEndDate());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<HabitualEventEntity> getTodayEvents(Long userId, Instant date) {
        Instant startOfDay = date.truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        Instant endOfDay = startOfDay.plus(java.time.Duration.ofDays(1));

        List<HabitualEventEntity> all = habitualEventRepository.findByUserId(userId);
        return all.stream()
                .filter(e ->
                        e.getStartDate() != null &&
                                e.getEndDate() != null &&
                                (e.getStartDate().isBefore(endOfDay) &&
                                        e.getEndDate().isAfter(startOfDay))
                )
                .collect(Collectors.toList());
    }
}
