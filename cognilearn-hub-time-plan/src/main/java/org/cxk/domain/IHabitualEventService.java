package org.cxk.domain;

import jakarta.validation.Valid;
import org.cxk.api.dto.HabitualEventCreateDTO;
import org.cxk.api.dto.HabitualEventUpdateDTO;
import org.cxk.api.response.HabitualEventVO;

import java.util.List;

/**
 * @author KJH
 * @description 日常事件（习惯性事件）领域服务接口
 * @create 2025/4/25 0:56
 */
public interface IHabitualEventService {

    /**
     * 删除日常事件（逻辑删除）
     */
    void deleteHabitualEvent(Long userId, Long eventId);

    Long createHabitualEvent(Long userId, @Valid HabitualEventCreateDTO dto);

    void updateHabitualEvent(Long userId, @Valid HabitualEventUpdateDTO dto);

    List<HabitualEventVO> listUserHabitualEvents(Long userId);
}