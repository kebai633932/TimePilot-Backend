package org.cxk.trigger.http;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.api.dto.*;
import org.cxk.api.response.AdHocEventVO;
import org.cxk.api.response.Response;
import org.cxk.domain.IAdHocEventService;
import org.cxk.types.enums.ResponseCode;
import org.cxk.util.AuthenticationUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
     * 查询突发事件列表
     */
    @PostMapping("/list")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<List<AdHocEventVO>> listAdHocEvents(@Valid @RequestBody AdHocEventQueryDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            List<AdHocEventVO> events = adHocEventService.listAdHocEvents(userId, dto);
            return Response.success(events, "查询成功");
        } catch (Exception e) {
            log.error("查询突发事件列表失败", e);
            return Response.error(ResponseCode.UN_ERROR, "查询突发事件列表失败");
        }
    }
    /**
     * 更新事件状态
     */
    @PostMapping("/update-status")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Boolean> updateEventStatus(@Valid @RequestBody EventStatusUpdateDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            adHocEventService.updateEventStatus(userId, dto.getEventId(), dto.getStatus());
            return Response.success(true, "事件状态更新成功");
        } catch (Exception e) {
            log.error("更新事件状态失败，id={}", dto.getEventId(), e);
            return Response.error(ResponseCode.UN_ERROR, "更新事件状态失败");
        }
    }


//    /**
//     * 根据ID查询突发事件详情
//     */
//    @GetMapping("/{eventId}")
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public Response<AdHocEventVO> getAdHocEvent(@PathVariable Long eventId) {
//        try {
//            Long userId = AuthenticationUtil.getCurrentUserId();
//            AdHocEventVO event = adHocEventService.getAdHocEventById(userId, eventId);
//            return Response.success(event, "查询成功");
//        } catch (Exception e) {
//            log.error("查询突发事件失败，id={}", eventId, e);
//            return Response.error(ResponseCode.UN_ERROR, "查询突发事件失败");
//        }
//    }



//    /**
//     * 记录实际开始/结束时间
//     */
//    @PostMapping("/record-actual-time")
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public Response<Boolean> recordActualTime(@Valid @RequestBody ActualTimeRecordDTO dto) {
//        try {
//            Long userId = AuthenticationUtil.getCurrentUserId();
//            adHocEventService.recordActualTime(userId, dto.getEventId(),
//                    dto.getActualStartTime(), dto.getActualEndTime(), dto.getActualHours());
//            return Response.success(true, "时间记录成功");
//        } catch (Exception e) {
//            log.error("记录实际时间失败，id={}", dto.getEventId(), e);
//            return Response.error(ResponseCode.UN_ERROR, "记录实际时间失败");
//        }
//    }
}