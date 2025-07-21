package org.cxk.service.handle;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author KJH
 * @description 自定义授权异常处理
 * @create 2025/7/18 10:49
 */
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    //todo
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 403
    }
}
