package org.cxk.trigger.http;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.api.dto.*;
import org.cxk.api.response.Response;
import org.cxk.domain.IHabitualEventService;
import org.cxk.types.enums.ResponseCode;
import org.cxk.util.AuthenticationUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description 日常事件（习惯性事件）管理
 */
@Slf4j
@RestController
@RequestMapping("/api/habitual-event")
@AllArgsConstructor
public class HabitualEventController {

    private final IHabitualEventService habitualEventService;

    /**
     * 创建日常事件
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Long> createHabitualEvent(@Valid @RequestBody HabitualEventCreateDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            Long eventId = habitualEventService.createHabitualEvent(userId, dto);
            return Response.success(eventId, "日常事件创建成功");
        } catch (Exception e) {
            log.error("创建日常事件失败，title={}", dto.getTitle(), e);
            return Response.error(ResponseCode.UN_ERROR, "创建日常事件失败");
        }
    }

    /**
     * 更新日常事件
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Boolean> updateHabitualEvent(@Valid @RequestBody HabitualEventUpdateDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            habitualEventService.updateHabitualEvent(userId, dto);
            return Response.success(true, "日常事件更新成功");
        } catch (Exception e) {
            log.error("更新日常事件失败，id={}", dto.getEventId(), e);
            return Response.error(ResponseCode.UN_ERROR, "更新日常事件失败");
        }
    }

    /**
     * 删除日常事件（逻辑删除）
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Boolean> deleteHabitualEvent(@Valid @RequestBody HabitualEventDeleteDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            habitualEventService.deleteHabitualEvent(userId, dto.getEventId());
            return Response.success(true, "日常事件删除成功");
        } catch (Exception e) {
            log.error("删除日常事件失败，id={}", dto.getEventId(), e);
            return Response.error(ResponseCode.UN_ERROR, "删除日常事件失败");
        }
    }

    /**
     * 查询日常事件列表
     */
    @PostMapping("/list")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<List<HabitualEventVO>> listHabitualEvents(@Valid @RequestBody HabitualEventQueryDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            List<HabitualEventVO> events = habitualEventService.listHabitualEvents(userId, dto);
            return Response.success(events, "查询成功");
        } catch (Exception e) {
            log.error("查询日常事件列表失败", e);
            return Response.error(ResponseCode.UN_ERROR, "查询日常事件列表失败");
        }
    }
    /**
     * 更新完成率
     */
    @PostMapping("/update-completion-rate")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Boolean> updateCompletionRate(@Valid @RequestBody CompletionRateUpdateDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            habitualEventService.updateCompletionRate(userId, dto.getEventId(), dto.getCompletionRate());
            return Response.success(true, "完成率更新成功");
        } catch (Exception e) {
            log.error("更新完成率失败，id={}", dto.getEventId(), e);
            return Response.error(ResponseCode.UN_ERROR, "更新完成率失败");
        }
    }


//    /**
//     * 根据ID查询日常事件详情
//     */
//    @GetMapping("/{eventId}")
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public Response<HabitualEventVO> getHabitualEvent(@PathVariable Long eventId) {
//        try {
//            Long userId = AuthenticationUtil.getCurrentUserId();
//            HabitualEventVO event = habitualEventService.getHabitualEventById(userId, eventId);
//            return Response.success(event, "查询成功");
//        } catch (Exception e) {
//            log.error("查询日常事件失败，id={}", eventId, e);
//            return Response.error(ResponseCode.UN_ERROR, "查询日常事件失败");
//        }
//    }


}