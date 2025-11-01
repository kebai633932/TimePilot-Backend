package org.cxk.domain;


import jakarta.validation.Valid;
import org.cxk.api.dto.AdHocEventCreateDTO;
import org.cxk.api.dto.AdHocEventUpdateDTO;
import org.cxk.api.response.AdHocEventVO;

import java.util.List;

/**
 * @author KJH
 * @description 突发事件领域服务接口
 * @create 2025/10/26 09:17
 */
public interface IAdHocEventService {

    /**
     * 删除突发事件（逻辑删除）
     *
     * @return
     */
    void deleteAdHocEvent(Long userId, Long eventId);

    void updateAdHocEvent(Long userId, @Valid AdHocEventUpdateDTO dto);

    Long createAdHocEvent(Long userId, @Valid AdHocEventCreateDTO dto);

    List<AdHocEventVO> listUserAdHocEvents(Long userId);
}