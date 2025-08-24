package org.cxk.util;

import org.cxk.trigger.dto.CustomUserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.cxk.types.exception.BizException;

/**
 * @author KJH
 * @description
 * @create 2025/8/14 11:37
 */
public class AuthenticationUtil {
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BizException("无认证用户");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDTO) {
            return ((CustomUserDTO) principal).getUserId();
        }

        // 如果是匿名用户
        if ("anonymousUser".equals(principal)) {
            throw new BizException("无认证用户");
        }

        throw new BizException("无认证用户");
    }
}
