package org.cxk.domain;



import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.cxk.api.dto.AdHocEventCreateDTO;
import org.cxk.api.dto.AdHocEventQueryDTO;
import org.cxk.api.dto.AdHocEventUpdateDTO;
import org.cxk.api.response.AdHocEventVO;
import org.cxk.infrastructure.adapter.dao.po.AdHocEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author KJH
 * @description 突发事件领域服务接口
 * @create 2025/10/26 09:17
 */
public interface IAdHocEventService {
    
    

    /**
     * 删除突发事件（逻辑删除）
     */
    void deleteAdHocEvent(Long userId, Long eventId);

    /**
     * 根据ID查询突发事件
     */
    AdHocEvent getAdHocEventById(Long userId, Long eventId);
    

//    /**
//     * 更新事件状态
//     */
//    void updateEventStatus(Long userId, Long eventId, EventStatus status);

    /**
     * 记录实际时间
     */
    void recordActualTime(Long userId, Long eventId, LocalDateTime actualStartTime,
                          LocalDateTime actualEndTime, BigDecimal actualHours);

    /**
     * 验证事件所有权
     */
    void validateEventOwnership(Long userId, Long eventId);

    /**
     * 获取用户突发事件统计
     */
    Map<String, Object> getUserAdHocEventStats(Long userId);

    /**
     * 查询今日待办突发事件
     */
    List<AdHocEvent> listTodayAdHocEvents(Long userId);

    /**
     * 查询逾期未完成事件
     */
    List<AdHocEvent> listOverdueAdHocEvents(Long userId);

    /**
     * 检查时间冲突
     */
    boolean hasTimeConflict(Long userId, LocalDateTime startTime, LocalDateTime endTime, Long excludeEventId);

//    /**
//     * 批量更新事件状态
//     */
//    void batchUpdateEventStatus(List<Long> eventIds, EventStatus status);

    /**
     * 关联习惯性事件
     */
    void linkToHabitualEvent(Long adHocEventId, Long habitualEventId);

    void updateAdHocEvent(Long userId, @Valid AdHocEventUpdateDTO dto);

    Long createAdHocEvent(Long userId, @Valid AdHocEventCreateDTO dto);

    List<AdHocEventVO> listAdHocEvents(Long userId, @Valid AdHocEventQueryDTO dto);

    void updateEventStatus(Long userId, @NotNull(message = "事件ID不能为空") Long eventId, @NotNull(message = "状态不能为空") @Min(value = 1, message = "状态值必须在1-4之间") @Max(value = 4, message = "状态值必须在1-4之间") Integer status);
}