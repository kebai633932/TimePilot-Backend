package org.cxk.domain;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.cxk.api.dto.AdHocEventCreateDTO;
import org.cxk.api.dto.AdHocEventUpdateDTO;
import org.cxk.api.response.AdHocEventVO;
import org.cxk.domain.model.entity.AdHocEventEntity;

import java.time.Instant;
import java.util.List;

/**
 * @author KJH
 * @description 突发事件领域服务接口
 * @create 2025/10/26 09:17
 */
public interface IAdHocEventService {

    void deleteAdHocEvent(Long userId, Long eventId);

    void updateAdHocEvent(Long userId, @Valid AdHocEventUpdateDTO dto);

    Long createAdHocEvent(Long userId, @Valid AdHocEventCreateDTO dto);

    List<AdHocEventVO> listUserAdHocEvents(Long userId);

    List<AdHocEventEntity> getTodayEvents(Long userId, @NotNull(message = "查询日期不能为空") Instant date
            ,@NotNull(message = "客户端地区不能为空") String clientTimeZone);
}