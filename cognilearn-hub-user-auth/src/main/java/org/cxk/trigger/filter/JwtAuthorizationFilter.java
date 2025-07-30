package org.cxk.trigger.filter;

import io.jsonwebtoken.Claims;
import org.cxk.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author KJH
 * @description JWT 授权过滤器
 * @create 2025/7/29 8:19
 */
//todo
@Component
public class JwtAuthorizationFilter{

    @Resource
    JwtUtil jwtUtil;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {


        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {

            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        // 从请求头中获取 Authorization 字段（格式一般是 "Bearer token"）
        String tokenHeader = request.getHeader("Authorization");

        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7); // 去掉 "Bearer " 前缀

            Claims claims = jwtUtil.parseToken(token);
            if (claims != null) {
                String username = claims.getSubject();

                if (username != null) {
                    // 你可以从 claims 中取出角色列表并转成权限，这里先用空权限列表
                    return new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                }
            }
        }
        return null;
    }

}
