package org.cxk.trigger.aop;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.cxk.trigger.dto.CustomUserDTO;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import types.enums.ResponseCode;
import types.exception.BizException;
import api.response.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 邮箱验证码发送限流切面
 *
 * <p>该切面实现了基于Redis Lua脚本的分布式限流功能，支持以下维度的限流控制：
 * 1. 邮箱发送频率限制（间隔时间）
 * 2. 邮箱小时发送次数限制
 * 3. 设备发送频率限制（间隔时间）
 * 4. 设备小时发送次数限制
 * 5. 系统每日总发送量限制
 * 6. 黑名单机制（触发限制后临时封禁）</p>
 *
 * @author KJH
 * @create 2025/8/2 15:58
 */
@Aspect
@Component
@Slf4j
public class EmailRateLimitAspect {
    private final RedissonClient redissonClient; // 使用RedissonClient替换StringRedisTemplate
    private final String rateLimitScript; // 存储Lua脚本字符串

    public EmailRateLimitAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.rateLimitScript = loadLuaScript("redis\\email_rate_limit.lua");
    }

    private String loadLuaScript(String path) {
        try {
            return StreamUtils.copyToString(
                    new ClassPathResource(path).getInputStream(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            log.error("Failed to load Lua script: {}", path, e);
            throw new BizException("限流系统初始化失败", e);
        }
    }

    /**
     * 环绕通知 - 处理带有@EmailRateLimit注解的方法
     *
     * <p>执行流程：
     * 1. 从请求参数中提取邮箱
     * 2. 从请求头中获取设备ID
     * 3. 执行Redis限流检查
     * 4. 根据限流结果决定放行或拦截请求</p>
     *
     * @param joinPoint 连接点对象
     * @param emailRateLimit 限流注解配置
     * @return 方法执行结果或限流错误响应
     * @throws Throwable 如果方法执行过程中出现异常
     */
    @Around("@annotation(emailRateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, EmailRateLimit emailRateLimit) throws Throwable {
        try {
            // 1. 获取邮箱和设备ID
            String email = getEmailFromArgs(joinPoint.getArgs());
            String deviceId = getDeviceIdFromRequest();

            // 2. 执行限流检查
            RateLimitResult result = executeRateLimitCheck(email, deviceId, emailRateLimit);

            // 3. 处理限流结果
            switch ((int) result.getStatus()) {
                //todo 用枚举类实现
                case -1: // 邮箱频率限制
                    return Response.error(ResponseCode.RATE_LIMITER, "邮箱操作频繁，请" + result.getTtl() + "秒后再试");
                case -2: // 设备频率限制
                    return Response.error(ResponseCode.RATE_LIMITER, "设备操作频繁，请" + result.getTtl() + "秒后再试");
                case -3: // 邮箱黑名单
                    return Response.error(ResponseCode.RATE_LIMITER, "邮箱操作受限，请明天再试");
                case -4: // 设备黑名单
                    return Response.error(ResponseCode.RATE_LIMITER, "设备操作受限，请明天再试");
                case -5: // 邮箱超过小时限制
                    return Response.error(ResponseCode.RATE_LIMITER, "该邮箱今日发送次数已达上限");
                case -6: // 设备超过小时限制
                    return Response.error(ResponseCode.RATE_LIMITER, "该设备今日发送次数已达上限");
                case -7: // 超过系统总量
                    return Response.error(ResponseCode.RATE_LIMITER, "系统邮件额度已用完");
                case 1: // 允许发送
                    log.debug("邮件验证码发送通过限流检查, 邮箱:{}, 设备:{}, 邮箱计数:{}, 设备计数:{}, 日总量:{}",
                            email, deviceId, result.getEmailCount(), result.getDeviceCount(), result.getDailyTotal());
                    return joinPoint.proceed();
                default:
                    log.error("未知限流状态: {}", result.getStatus());
                    return Response.error(ResponseCode.UN_ERROR, "系统限流异常");
            }
        } catch (BizException e) {
            log.error("限流处理异常: {}", e.getMessage());
            return Response.error(ResponseCode.UN_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("系统异常: {}", e.getMessage(), e);
            return Response.error(ResponseCode.UN_ERROR, "系统处理异常");
        }
    }

    /**
     * 执行Redis限流检查
     *
     * <p>构建Redis Key并调用Lua脚本执行限流逻辑</p>
     *
     * @param email 用户邮箱
     * @param deviceId 设备ID
     * @param emailRateLimit 限流配置注解
     * @return 限流结果对象
     */
    private RateLimitResult executeRateLimitCheck(String email, String deviceId, EmailRateLimit emailRateLimit) {
        // 构建Redis Key
        String keyPrefix = emailRateLimit.key();
        String emailIntervalKey = keyPrefix + "interval:email:" + email;
        String emailHourlyKey = keyPrefix + "hourly:email:" + email; // 滑动窗口ZSet

        // 获取当天日期字符串 (格式: yyyyMMdd)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String today = sdf.format(new Date());
        String emailBlacklistKey = keyPrefix + "blacklist:email:" + today;
        String deviceBlacklistKey = keyPrefix + "blacklist:device:" + today;

        String deviceIntervalKey = keyPrefix + "interval:device:" + deviceId;
        String deviceHourlyKey = keyPrefix + "hourly:device:" + deviceId; // 滑动窗口ZSet
        String dailyTotalKey = keyPrefix + "daily_total";

        // 准备KEYS列表（必须转为List<Object>）
        List<Object> keys = Arrays.asList(
                emailIntervalKey, emailHourlyKey, emailBlacklistKey,
                deviceIntervalKey, deviceHourlyKey, deviceBlacklistKey,
                dailyTotalKey
        );

        // 获取当前时间戳（秒）
        long currentTimestamp = System.currentTimeMillis() / 1000;
// 生成唯一请求ID
        String requestId = UUID.randomUUID().toString();
        // 准备ARGV参数
        List<Object> argv = Arrays.asList(
                emailRateLimit.emailHourlyLimit(),  // ARGV[1] 邮箱每小时最大次数
                emailRateLimit.emailInterval(),     // ARGV[2] 邮箱间隔秒数
                emailRateLimit.deviceHourlyLimit(), // ARGV[3] 设备每小时最大次数
                emailRateLimit.deviceInterval(),    // ARGV[4] 设备间隔秒数
                emailRateLimit.dailyTotalLimit(),   // ARGV[5] 每日最大总量
                currentTimestamp,                  // ARGV[6] 当前时间戳
                emailRateLimit.blacklistTtl(),     // ARGV[7] 黑名单过期时间
                email,                             // ARGV[8] 邮箱地址
                deviceId,                          // ARGV[9] 设备ID
                requestId                          // ARGV[10] 唯一请求ID
        );

        // 使用Redisson执行脚本
        RScript script = redissonClient.getScript();
        List<Long> result;
        try {
            // 正确的eval方法调用
            result = script.eval(
                    RScript.Mode.READ_WRITE,  // Redis 模式：读写模式
                    rateLimitScript,
                    RScript.ReturnType.MULTI,  // 脚本返回多个值（return {...}）
                    keys,   // KEYS列表
                    argv.toArray()  // ARGV参数数组
            );
        } catch (Exception e) {
            log.error("限流系统异常: {}", e.getMessage(), e);
            // 降级放行
            return new RateLimitResult(1, 0, 0, 0, 0);
        }

        if (result == null || result.isEmpty()) {
            log.error("限流脚本返回空结果");
            return new RateLimitResult(1, 0, 0, 0, 0);
        }

        return new RateLimitResult(
                result.get(0),                         // 限流状态码
                result.size() > 1 ? result.get(1) : 0, // 剩余时间（秒）
                result.size() > 2 ? result.get(2) : 0, // 当前邮箱计数
                result.size() > 3 ? result.get(3) : 0, // 当前设备计数
                result.size() > 4 ? result.get(4) : 0  // 系统日总量计数
        );
    }

    private String getDeviceIdFromRequest() {
        // 1. 尝试从安全上下文中获取（登录后场景）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            // 1.1 如果是CustomUserDTO类型（登录用户）
            if (principal instanceof CustomUserDTO) {
                CustomUserDTO customUser = (CustomUserDTO) principal;
                String deviceId = customUser.getDeviceId();
                if (StringUtils.isNotBlank(deviceId)) {
                    return deviceId;
                }
            }
            // 1.2 如果是JwtAuthenticationToken类型
        }

        // 2. 从请求头中获取（未登录场景）,注册验证码
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            String deviceId = attrs.getRequest().getHeader("X-Device-Id");
            if (StringUtils.isBlank(deviceId)) {
                throw new BizException("请求中缺少设备ID");
            }
            return deviceId;
        }

        throw new BizException("无法获取请求上下文");
    }
    /**
     * 从方法参数中提取邮箱地址
     *
     * <p>支持两种参数类型：
     * 1. 直接字符串参数（包含@和.符号）
     * 2. EmailRequest对象（需包含getEmail()方法）</p>
     *
     * @param args 方法参数数组
     * @return 提取的邮箱地址
     * @throws BizException 如果无法找到有效的邮箱参数
     */
    private String getEmailFromArgs(Object[] args) {
        // 根据实际方法参数结构获取邮箱
        for (Object arg : args) {
            if (arg instanceof String) {
                //todo 邮箱格式验证
                String str = (String) arg;
                // 简单邮箱格式验证
                if (str.contains("@") && str.contains(".")) {
                    return str;
                }
            }
        }
        throw new BizException("请求中缺少邮箱参数");
    }


    /**
     * 将秒数格式化为友好的时间字符串
     *
     * <p>转换规则：
     * - 小于60秒：显示为"X秒"
     * - 60秒~1小时：显示为"X分钟"
     * - 大于1小时：显示为"X小时"</p>
     *
     * @param seconds 时间秒数
     * @return 格式化后的时间字符串
     */
    private String formatTime(long seconds) {
        // 转换时间为友好格式
        if (seconds < 60) return seconds + "秒";
        if (seconds < 3600) return (seconds / 60) + "分钟";
        return (seconds / 3600) + "小时";
    }

    /**
     * 限流结果封装类
     *
     * <p>用于存储Lua脚本返回的限流检查结果</p>
     */
    @Data
    @AllArgsConstructor
    private static class RateLimitResult {
        /** 限流状态码 */
        private long status;

        /** 剩余时间（秒） */
        private long ttl;

        /** 当前邮箱计数 */
        private long emailCount;

        /** 当前设备计数 */
        private long deviceCount;

        /** 系统日总量计数 */
        private long dailyTotal;
    }
}