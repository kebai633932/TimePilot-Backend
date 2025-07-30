package org.cxk.trigger.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.cxk.trigger.dto.CustomUserDTO;
import org.cxk.trigger.dto.UserLoginDTO;
import org.cxk.trigger.exception.UsernameNotExistsException;
import org.cxk.util.JwtUtil;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Resource
    private JwtUtil jwtUtil;
    @Resource
    RedisUserBloomFilter redisUserBloomFilter;

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
            // 布隆过滤器未创建
            if (redisUserBloomFilter == null) {
                throw new RuntimeException("布隆过滤器出错，未创建");
            }
            // 布隆过滤器快速判断用户名是否存在，减少无效数据库查询
            if (!redisUserBloomFilter.mightContain(loginDTO.getUsername())) {
                // 用户名不存在，直接抛异常，拦截请求
                throw new UsernameNotExistsException("用户名不存在（布隆过滤器拦截）");
            }

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
        //1. 将Authentication 保存到 线程上下文
        SecurityContextHolder.getContext().setAuthentication(authResult);

        Long userId = customUser.getId();

        // 2. 从请求头中获取 deviceId（前端必须传）
        String deviceId = request.getHeader("X-Device-Id");
        if (deviceId == null || deviceId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            //400: 客户端请求有错误，服务器无法理解或处理该请求
            response.getWriter().write("{\"code\":400,\"message\":\"缺少设备ID（X-Device-Id）\"}");
            return;
        }

        // 3. 生成 Token
        String accessToken = jwtUtil.generateAccessToken(userId, customUser.getAuthorities());
        String refreshToken = jwtUtil.generateRefreshToken(userId, customUser.getAuthorities());

        long accessTokenExpire = jwtUtil.getAccessTokenExpiration();
        long refreshTokenExpire = jwtUtil.getRefreshTokenExpiration();

        // 4. 存入 Redis，key 带上 deviceId
        redissonClient.getBucket("login:token:" + userId + ":" + deviceId)
                .set(accessToken, accessTokenExpire, TimeUnit.SECONDS);

        redissonClient.getBucket("login:refresh:" + userId + ":" + deviceId)
                .set(refreshToken, refreshTokenExpire, TimeUnit.DAYS);

        // 5. 记录设备 ID（方便全局登出）
        redissonClient.getSet("login:session:" + userId).add(deviceId);

        // 6. 返回响应
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "登录成功");
        result.put("access_token", accessToken);
        result.put("refresh_token", refreshToken);
//        result.put("device_id", deviceId);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }



    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException e)
            throws IOException, ServletException {
        Map<String, Object> error = new HashMap<>();
        error.put("code", 401);
        error.put("message", e.getMessage() != null ? e.getMessage() : "用户名或密码错误");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(error));
    }
}
