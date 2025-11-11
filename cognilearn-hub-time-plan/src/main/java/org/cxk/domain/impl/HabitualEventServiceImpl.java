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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        event.setRepeatPattern(dto.getRepeatPattern());
        event.setRepeatInterval(dto.getRepeatInterval());
        event.setEstimatedTime(dto.getEstimatedTime());
        event.setDescription(dto.getDescription());
        event.setPreferredTimeSlots(dto.getPreferredTimeSlots());
        event.setMeasurementUnit(dto.getMeasurementUnit());
        event.setTargetQuantity(dto.getTargetQuantity());
        event.setCompletedQuantity(BigDecimal.ZERO); // 初始化为 0
        event.setCompletionRate(BigDecimal.ZERO);   // 初始化为 0%

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

        // ===== 映射字段（仅更新有意义的部分）=====
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getQuadrant() != null) {
            event.setQuadrant(dto.getQuadrant());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getEstimatedTime() != null) {
            event.setEstimatedTime(dto.getEstimatedTime());
        }
        if (dto.getPreferredTimeSlots() != null) {
            event.setPreferredTimeSlots(dto.getPreferredTimeSlots());
        }
        if (dto.getRepeatPattern() != null) {
            event.setRepeatPattern(dto.getRepeatPattern());
        }
        if (dto.getRepeatInterval() != null) {
            event.setRepeatInterval(dto.getRepeatInterval());
        }
        if (dto.getMeasurementUnit() != null) {
            event.setMeasurementUnit(dto.getMeasurementUnit());
        }
        if (dto.getTargetQuantity() != null) {
            event.setTargetQuantity(dto.getTargetQuantity());
        }
        if (dto.getCompletedQuantity() != null) {
            event.setCompletedQuantity(dto.getCompletedQuantity());
        }

        // 自动计算完成率
        if (event.getTargetQuantity() != null
                && event.getCompletedQuantity() != null
                && event.getTargetQuantity().compareTo(BigDecimal.ZERO) > 0) {
            event.setCompletionRate(
                    event.getCompletedQuantity()
                            .divide(event.getTargetQuantity(), 2, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
            );
        }

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
            vo.setId(e.getId());
            vo.setUserId(e.getUserId());
            vo.setTitle(e.getTitle());
            vo.setQuadrant(e.getQuadrant());
            vo.setEstimatedTime(e.getEstimatedTime());
            vo.setDescription(e.getDescription());
            vo.setPreferredTimeSlots(e.getPreferredTimeSlots());
            vo.setRepeatPattern(e.getRepeatPattern());
            vo.setRepeatInterval(e.getRepeatInterval());
            vo.setCompletionRate(e.getCompletionRate());
            vo.setMeasurementUnit(e.getMeasurementUnit());
            vo.setTargetQuantity(e.getTargetQuantity());
            vo.setCompletedQuantity(e.getCompletedQuantity());

            return vo;
        }).collect(Collectors.toList());
    }

}
