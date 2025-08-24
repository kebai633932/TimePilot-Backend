package org.cxk.trigger.filter;

import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cxk.trigger.dto.CustomUserDTO;
import org.cxk.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.cxk.types.exception.BizException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器
 * @author KJH
 * @create 2025/7/28 15:55
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. 从请求头中获取JWT令牌
            String jwt = jwtUtil.parseJwt(request);

            // 2. 校验token有效性（格式、签名、是否过期）
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                // 3. 解析JWT获取claims
                Claims claims = jwtUtil.parseToken(jwt);
                // 3.1 提取字段
                String username = claims.getSubject();
                Long userId = claims.get("userId", Long.class);
                String deviceId = claims.get("deviceId", String.class);
                List<String> roleList = extractRoles(claims);
                // 3.2 校验字段是否缺失
                if (username == null || userId == null || deviceId == null) {
                    throw new BizException(String.format(
                            "Token中缺失必要字段: username=%s, userId=%s, deviceId=%s",
                            username, userId, deviceId
                    ));
                }

                // 3.3 构建权限
                Collection<? extends GrantedAuthority> authorities = convertToAuthorities(roleList);

                // 4. Redis 黑名单校验
                if (jwtUtil.isTokenBlacklisted(claims)) {
                    logger.warn("JWT 被列入黑名单: {}"+ jwt);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401, \"message\":\"登录状态已失效，请重新登录\"}");
                    return;
                }

                // 5. 加载用户信息（UserDetails）, 没有要求严格的权限实时性
//                CustomUserDTO customUser = (CustomUserDTO) userDetailsServiceImpl.loadUserByUsername(username);
                CustomUserDTO customUser = new CustomUserDTO(userId,username,"",deviceId,authorities);
                // 6. 构建认证对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                customUser,
                                null,
                                customUser.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. 将认证信息写入上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            logger.error("无法设置用户认证: {}",e);
        }

        // 8. 放行请求
        filterChain.doFilter(request, response);
    }
    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Claims claims) {
        Object roleObj = claims.get("roles");
        if (roleObj instanceof List<?>) {
            List<?> roleList = (List<?>) roleObj;

            // 校验每一项是否为 String 类型
            if (roleList.isEmpty() || roleList.get(0) instanceof String) {
                return (List<String>) roleList;
            }
        }
        throw new BizException("Token中角色字段格式非法");
    }
    private Collection<? extends GrantedAuthority> convertToAuthorities(List<String> roles) {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
