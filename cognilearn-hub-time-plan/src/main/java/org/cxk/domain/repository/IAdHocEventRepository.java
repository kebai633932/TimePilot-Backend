package org.cxk.domain.repository;

import org.cxk.domain.model.entity.AdHocEventEntity;
import org.cxk.infrastructure.adapter.dao.po.AdHocEvent;

import java.util.List;

/**
 * @author KJH
 * @description 突发事件仓储接口
 * @create 2025/10/26 09:34
 */
public interface IAdHocEventRepository {

    /**
     * 根据用户ID查询突发事件列表
     */
    List<AdHocEventEntity> findByUserId(Long userId);

    /**
     * 根据事件ID查询突发事件
     */
    AdHocEvent findById(Long eventId);

    /**
     * 新增突发事件
     */
    void save(AdHocEvent event);

    /**
     * 更新突发事件
     */
    void update(AdHocEvent event);

    /**
     * 删除突发事件
     */
    void delete(Long userId, Long eventId);
}
