package org.cxk.trigger.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.cxk.trigger.dto.CustomUserDTO;
import org.cxk.trigger.dto.UserLoginDTO;
import org.cxk.util.JwtUtil;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    @Setter
    private JwtUtil jwtUtil;
    @Resource
    RedissonClient redissonClient;

    public JwtLoginFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        // 设置登录请求路径和方法
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/user/auth/login", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            UserLoginDTO loginDTO = new ObjectMapper().readValue(request.getInputStream(), UserLoginDTO.class);
            Authentication token = new UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(),
                    loginDTO.getPassword()
            );
            return authenticationManager.authenticate(token);
        } catch (IOException e) {
            throw new RuntimeException("登录请求数据格式错误", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        CustomUserDTO customUser = (CustomUserDTO) authResult.getPrincipal();

        if (jwtUtil == null) {
            throw new IllegalStateException("JwtUtil 未注入");
        }

        Long userId = customUser.getId();
        String accessToken = jwtUtil.generateAccessToken(userId, customUser.getAuthorities());
        String refreshToken = jwtUtil.generateRefreshToken(userId, customUser.getAuthorities());

        // 1. 写入 Redis（设置过期时间，与 JWT 保持一致）
        // AccessToken有效期（分钟）
        // RefreshToken有效期（天）
        long accessTokenExpire = jwtUtil.getAccessTokenExpiration();
        long refreshTokenExpire = jwtUtil.getRefreshTokenExpiration();

        redissonClient.getBucket("login:token:" + userId).set(accessToken, accessTokenExpire, TimeUnit.SECONDS);
        redissonClient.getBucket("login:refresh:" + userId).set(refreshToken, refreshTokenExpire, TimeUnit.DAYS);

        // 2. 返回响应
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "登录成功");
        result.put("access_token", accessToken);
        result.put("refresh_token", refreshToken);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException e)
            throws IOException, ServletException {
        Map<String, Object> error = new HashMap<>();
        error.put("code", 401);
        error.put("message", "用户名或密码错误");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(error));
    }
}
