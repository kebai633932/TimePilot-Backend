package org.cxk.trigger.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.cxk.trigger.dto.CustomUserDTO;
import org.cxk.trigger.dto.UserLoginDTO;
import org.cxk.util.JwtUtil;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisTimeoutException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.cxk.types.exception.BizException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    @Resource
    private JwtUtil jwtUtil;
    @Resource
    RedisUserBloomFilter redisUserBloomFilter;

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

            if (redisUserBloomFilter == null) {
                throw new RuntimeException("布隆过滤器未创建");
            }

            try {
                if (!redisUserBloomFilter.mightContain(loginDTO.getUsername())) {
                    throw new BizException("用户名不存在（布隆过滤器拦截）");
                }
            } catch (RedisConnectionException | RedisTimeoutException e) {
                log.error("Redis异常，跳过布隆过滤器校验: {}", e.getMessage());
                // Redis不可用抛的异常，放行继续认证
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
            throws IOException {
        CustomUserDTO customUser = (CustomUserDTO) authResult.getPrincipal();

        if (jwtUtil == null) {
            throw new IllegalStateException("JwtUtil 未注入");
        }

        // 1. 从请求头中获取 deviceId（前端必须传）
        // todo 前端，deviceId需要持久化到localStorage
        String deviceId = request.getHeader("X-Device-Id");
        if (deviceId == null || deviceId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            //400: 客户端请求有错误，服务器无法理解或处理该请求
            response.getWriter().write("{\"code\":400,\"message\":\"缺少设备ID（X-Device-Id）\"}");
            return;
        }
        //2. 重构Authentication，并保存到 线程上下文
        customUser.setDeviceId(deviceId);
        //调用了这个构造函数，并且 authorities 不为 null，对象就会默认 authenticated = true
        UsernamePasswordAuthenticationToken authenticationResult= new UsernamePasswordAuthenticationToken(customUser,authResult.getCredentials(),authResult.getAuthorities());
        authenticationResult.setDetails(authResult.getDetails());

        SecurityContextHolder.getContext().setAuthentication(authenticationResult);

        String username = customUser.getUsername();
        Long userId = customUser.getUserId();
        List<String> roleList = extractRoles(customUser);
        // 生成JTI和令牌
        String jti = UUID.randomUUID().toString();
        String accessToken = jwtUtil.generateAccessToken(
                username,
                roleList,
                deviceId,
                userId
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                username,
                jti,
                roleList,
                deviceId,
                userId
        );

        // 保存JTI到Redis
        jwtUtil.saveRefreshTokenJti(userId, deviceId, jti);
//
//        // 5. 记录设备 ID（方便全局登出）
//        redissonClient.getSet("login:session:" + userId).add(deviceId);

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

    private List<String> extractRoles(CustomUserDTO customUser) {
        Collection<? extends GrantedAuthority> authorities = customUser.getAuthorities();
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException e)
            throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("code", 401);
        error.put("message", e.getMessage() != null ? e.getMessage() : "用户名或密码错误");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(error));
    }
}
