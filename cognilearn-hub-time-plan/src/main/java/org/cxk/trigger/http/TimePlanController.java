package org.cxk.trigger.http;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.domain.IAdHocEventService;
import org.cxk.domain.IHabitualEventService;
import org.cxk.util.AuthenticationUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author KJH
 * @description 时间安排管理（智能规划）
 * @create 2025/10/26
 */
@Slf4j
@RestController
@RequestMapping("/api/time-plan")
@AllArgsConstructor
public class TimePlanController {

    private final IAdHocEventService adHocEventService;
    private final IHabitualEventService habitualEventService;

    /**
     * 智能规划当日时间安排
     */
    @PostMapping("/smart-daily-plan")
    @PreAuthorize("hasAuthority('event:plan:smart')")
    public void generateSmartDailyPlan() {
        Long userId = AuthenticationUtil.getCurrentUserId();

        try {
            // 1.获取当日突发事件

            // ️ 2.异步部分智能排布（调度），如果有冲突报错

            // 3.获取当日习惯事件

            // 4.在已经调度的基础上分配时间

            // 5️.封装结果

            return ;

        } catch (Exception e) {
            log.error("[智能规划失败] userId={}, err={}", userId, e.getMessage(), e);
            return ;
        }
    }

    //多维度时间查询接口（日 / 周 / 月 / 年）
    //构建时间表（天 / 周）
    //构建统计视图（月 / 年）
}
