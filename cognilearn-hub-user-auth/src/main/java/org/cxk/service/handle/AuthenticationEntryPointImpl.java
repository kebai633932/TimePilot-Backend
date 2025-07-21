package org.cxk.service.handle;


import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * @author KJH
 * @description 自定义认证异常处理
 * @create 2025/7/18 10:48
 */
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    //todo
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // todo HttpStatus.
        // 401
        HttpStatus
    }
}
