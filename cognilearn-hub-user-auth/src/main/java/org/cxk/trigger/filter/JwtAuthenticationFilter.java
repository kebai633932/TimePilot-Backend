package org.cxk.trigger.filter;
import io.jsonwebtoken.Claims;
import org.cxk.service.impl.CustomUserDetailsServiceImpl;
import org.cxk.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
/**
 * @author KJH
 * @description JWT 认证过滤器
 * @create 2025/7/28 15:55
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsServiceImpl userDetailsServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. 从请求头中获取JWT令牌
            String jwt = parseJwt(request);

            if (jwt != null && jwtUtil.validateToken(jwt)) {
                // 2. 解析JWT获取用户信息
                Claims claims = jwtUtil.parseToken(jwt);
                String username = claims.getSubject();

                // 3. 加载用户详细信息
                UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);

                // 4. 构建认证对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 5. 设置认证上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("无法设置用户认证: {}", e);
        }

        // 6. 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中解析JWT令牌
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}

