package org.cxk.domain.repository;

import org.cxk.domain.model.entity.HabitualEventEntity;
import org.cxk.infrastructure.adapter.dao.po.HabitualEvent;

import java.util.List;

/**
 * @author KJH
 * @description 习惯性事件仓储接口
 * @create 2025/10/26 09:34
 */
public interface IHabitualEventRepository {

    /**
     * 根据用户ID查询习惯性事件列表
     */
    List<HabitualEventEntity> findByUserId(Long userId);

    /**
     * 根据事件ID查询习惯性事件
     */
    HabitualEvent findById(Long eventId);

    /**
     * 新增习惯性事件
     */
    void save(HabitualEvent event);

    /**
     * 更新习惯性事件
     */
    void update(HabitualEvent event);

    /**
     * 删除习惯性事件
     */
    void delete(Long eventId);
}
