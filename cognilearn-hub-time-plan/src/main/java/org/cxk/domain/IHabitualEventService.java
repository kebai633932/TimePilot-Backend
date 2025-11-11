package org.cxk.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.cxk.api.dto.HabitualEventCreateDTO;
import org.cxk.api.dto.HabitualEventUpdateDTO;
import org.cxk.api.response.HabitualEventVO;
import org.cxk.domain.model.entity.HabitualEventEntity;

import java.time.Instant;
import java.util.List;

/**
 * @author KJH
 * @description 日常事件（习惯性事件）领域服务接口
 * @create 2025/4/25 0:56
 */
public interface IHabitualEventService {

    void deleteHabitualEvent(Long userId, Long eventId);

    Long createHabitualEvent(Long userId, @Valid HabitualEventCreateDTO dto);

    void updateHabitualEvent(Long userId, @Valid HabitualEventUpdateDTO dto);

    List<HabitualEventVO> listUserHabitualEvents(Long userId);

}