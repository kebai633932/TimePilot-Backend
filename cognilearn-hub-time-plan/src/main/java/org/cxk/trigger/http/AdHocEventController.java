package org.cxk.trigger.http;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.api.dto.AdHocEventCreateDTO;
import org.cxk.api.dto.AdHocEventDeleteDTO;
import org.cxk.api.dto.AdHocEventUpdateDTO;
import org.cxk.api.response.AdHocEventVO;
import org.cxk.api.response.Response;
import org.cxk.domain.IAdHocEventService;
import org.cxk.types.enums.ResponseCode;
import org.cxk.util.AuthenticationUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description 突发事件管理
 */
@Slf4j
@RestController
@RequestMapping("/api/ad-hoc-event")
@AllArgsConstructor
public class AdHocEventController {

    private final IAdHocEventService adHocEventService;

    /**
     * 创建突发事件
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Long> createAdHocEvent(@Valid @RequestBody AdHocEventCreateDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            Long eventId = adHocEventService.createAdHocEvent(userId, dto);
            return Response.success(eventId, "突发事件创建成功");
        } catch (Exception e) {
            log.error("创建突发事件失败，title={}", dto.getTitle(), e);
            return Response.error(ResponseCode.UN_ERROR, "创建突发事件失败");
        }
    }

    /**
     * 更新突发事件
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Boolean> updateAdHocEvent(@Valid @RequestBody AdHocEventUpdateDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            adHocEventService.updateAdHocEvent(userId, dto);
            return Response.success(true, "突发事件更新成功");
        } catch (Exception e) {
            log.error("更新突发事件失败，id={}", dto.getEventId(), e);
            return Response.error(ResponseCode.UN_ERROR, "更新突发事件失败");
        }
    }

    /**
     * 删除突发事件（逻辑删除）
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Boolean> deleteAdHocEvent(@Valid @RequestBody AdHocEventDeleteDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            adHocEventService.deleteAdHocEvent(userId, dto.getEventId());
            return Response.success(true, "突发事件删除成功");
        } catch (Exception e) {
            log.error("删除突发事件失败，id={}", dto.getEventId(), e);
            return Response.error(ResponseCode.UN_ERROR, "删除突发事件失败");
        }
    }

    /**
     * 查看突发事件列表
     */
    @PostMapping("/list")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<List<AdHocEventVO>> listAdHocEvents() {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            List<AdHocEventVO> list = adHocEventService.listUserAdHocEvents(userId);
            return Response.success(list, "突发事件获取成功");
        } catch (Exception e) {
            log.error("获取突发事件列表失败", e);
            return Response.error(ResponseCode.UN_ERROR, "获取突发事件列表失败");
        }
    }
}