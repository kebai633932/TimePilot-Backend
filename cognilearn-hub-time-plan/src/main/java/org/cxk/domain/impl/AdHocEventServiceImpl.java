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
     * @return
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
        AdHocEvent event = adHocEventRepository.findById(dto.getEventId());
        Assert.notNull(event, "事件不存在");
        Assert.isTrue(event.getUserId().equals(userId), "无权修改他人事件");

        event.setTitle(dto.getTitle());
        event.setQuadrant(dto.getQuadrant());
        event.setPlannedStartTime(dto.getPlannedStartTime());
        event.setPlannedEndTime(dto.getPlannedEndTime());

        adHocEventRepository.update(event);
    }

    /**
     * 创建突发事件
     */
    @Override
    public Long createAdHocEvent(Long userId, AdHocEventCreateDTO dto) {
        AdHocEvent event = new AdHocEvent();
        event.setUserId(userId);
        event.setTitle(dto.getTitle());
        event.setQuadrant(dto.getQuadrant());
        event.setPlannedStartTime(dto.getPlannedStartTime());
        event.setPlannedEndTime(dto.getPlannedEndTime());
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
            vo.setEventId(e.getId());
            vo.setTitle(e.getTitle());
            vo.setQuadrant(e.getQuadrant());
            vo.setPlannedStartTime(e.getPlannedStartTime());
            vo.setPlannedEndTime(e.getPlannedEndTime());
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
