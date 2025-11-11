package org.cxk.domain.impl;

import jakarta.annotation.Resource;
import org.cxk.api.dto.AdHocEventCreateDTO;
import org.cxk.api.dto.AdHocEventUpdateDTO;
import org.cxk.api.response.AdHocEventVO;
import org.cxk.domain.IAdHocEventService;
import org.cxk.domain.model.entity.AdHocEventEntity;
import org.cxk.domain.repository.IAdHocEventRepository;
import org.cxk.infrastructure.adapter.dao.po.AdHocEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author KJH
 * @description 突发事件领域服务实现
 */
@Service
public class AdHocEventServiceImpl implements IAdHocEventService {

    @Resource
    private IAdHocEventRepository adHocEventRepository;

    /**
     * 删除突发事件
     *
     */
    @Override
    public void deleteAdHocEvent(Long userId, Long eventId) {
        adHocEventRepository.delete(userId,eventId);
    }

    /**
     * 更新突发事件
     */
    @Override
    public void updateAdHocEvent(Long userId, AdHocEventUpdateDTO dto) {
        // 1️⃣ 查询事件
        AdHocEvent event = adHocEventRepository.findById(dto.getId());
        Assert.notNull(event, "事件不存在");
        Assert.isTrue(event.getUserId().equals(userId), "无权修改他人事件");

        // 2️⃣ 更新基本字段
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getQuadrant() != null) {
            event.setQuadrant(dto.getQuadrant());
        }
        if (dto.getPlannedStartTime() != null) {
            event.setPlannedStartTime(dto.getPlannedStartTime());
        }
        if (dto.getPlannedEndTime() != null) {
            event.setPlannedEndTime(dto.getPlannedEndTime());
        }
        if (dto.getDeadline() != null) {
            event.setDeadline(dto.getDeadline());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
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
        if (dto.getStatus() != null) {
            event.setStatus(dto.getStatus());
        }

        // 4️⃣ 数据校验（可选，比如开始时间 < 结束时间）
        if (event.getPlannedStartTime() != null && event.getPlannedEndTime() != null) {
            Assert.isTrue(!event.getPlannedStartTime().isAfter(event.getPlannedEndTime()), "计划开始时间不能晚于结束时间");
        }

        // 5️⃣ 持久化更新
        adHocEventRepository.update(event);
    }


    /**
     * 创建突发事件
     */
    @Override
    public Long createAdHocEvent(Long userId, AdHocEventCreateDTO dto) {
        AdHocEvent event = new AdHocEvent();
        //dto
        event.setUserId(userId);
        event.setTitle(dto.getTitle());
        event.setQuadrant(dto.getQuadrant());
        event.setPlannedStartTime(dto.getPlannedStartTime());
        event.setPlannedEndTime(dto.getPlannedEndTime());
        event.setDeadline(dto.getDeadline());
        event.setDescription(dto.getDescription());
        event.setMeasurementUnit(dto.getMeasurementUnit());
        event.setTargetQuantity(dto.getTargetQuantity());
        event.setStatus(dto.getStatus());

        adHocEventRepository.save(event);
        return event.getId();
    }

    /**
     * 查询用户突发事件列表
     */
    @Override
    public List<AdHocEventVO> listUserAdHocEvents(Long userId) {
        List<AdHocEventEntity> list = adHocEventRepository.findByUserId(userId);
        return list.stream().map(e -> {
            AdHocEventVO vo = new AdHocEventVO();
            vo.setId(e.getId());
            vo.setUserId(e.getUserId());
            vo.setTitle(e.getTitle());
            vo.setQuadrant(e.getQuadrant());
            vo.setPlannedStartTime(e.getPlannedStartTime());
            vo.setPlannedEndTime(e.getPlannedEndTime());
            vo.setDeadline(e.getDeadline());
            vo.setDescription(e.getDescription());
            vo.setStatus(e.getStatus());
            vo.setMeasurementUnit(e.getMeasurementUnit());
            vo.setTargetQuantity(e.getTargetQuantity());
            vo.setCompletedQuantity(e.getCompletedQuantity());
            return vo;
        }).collect(Collectors.toList());
    }


    @Override
    public List<AdHocEventEntity> getTodayEvents(Long userId, Instant date) {
        // 1️⃣ 计算当天起止时间
        Instant startOfDay = date.truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        Instant endOfDay = startOfDay.plus(java.time.Duration.ofDays(1));

        // 2️⃣ 查询当日事件（开始或结束时间在当天内）
        List<AdHocEventEntity> all = adHocEventRepository.findByUserId(userId);
        return all.stream()
                .filter(e ->
                        e.getPlannedStartTime() != null &&
                                e.getPlannedEndTime() != null &&
                                (e.getPlannedStartTime().isBefore(endOfDay) &&
                                        e.getPlannedEndTime().isAfter(startOfDay))
                )
                .collect(Collectors.toList());
    }
}
