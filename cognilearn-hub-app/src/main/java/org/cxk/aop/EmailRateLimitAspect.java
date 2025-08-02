package org.cxk.aop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import types.exception.BizException;

import java.util.Arrays;
import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/8/2 15:58
 */
@Aspect
@Component
@Slf4j
public class EmailRateLimitAspect {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List<Long>> rateLimitScript;

    public EmailRateLimitAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        // 加载Lua脚本
        this.rateLimitScript = RedisScript.of(
                ResourceUtils.getResourceAsString("classpath:lua/email_rate_limit.lua"),
                List.class
        );
    }

    @Around("@annotation(emailRateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, EmailRateLimit emailRateLimit) throws Throwable {
        // 1. 获取方法参数中的邮箱地址
        String email = getEmailFromArgs(joinPoint.getArgs());
        if (email == null) {
            throw new IllegalArgumentException("未找到邮箱地址参数");
        }

        // 2. 执行限流检查
        RateLimitResult result = executeRateLimitCheck(email, emailRateLimit);

        // 3. 根据结果处理
        switch ((int) result.getStatus()) {
            case -1: // 频率限制
                throw new BizException("操作频繁，请" + result.getTtl() + "秒后再试");
            case -2: // 黑名单中
                throw new BizException("操作受限，请" + formatTime(result.getTtl()) + "后再试");
            case -3: // 超过小时限制
                throw new BizException("今日发送次数已达上限");
            case -4: // 超过系统总量
                throw new BizException("系统邮件额度已用完");
            case 1: // 允许发送
                log.info("邮件验证码发送通过限流检查, 邮箱:{}, 小时计数:{}, 日总量:{}",
                        email, result.getHourlyCount(), result.getDailyTotal());
                return joinPoint.proceed();
            default:
                throw new BizException("未知限流状态: " + result.getStatus());
        }
    }

    private RateLimitResult executeRateLimitCheck(String email, EmailRateLimit emailRateLimit) {
        // 构建Redis Key
        String keyPrefix = emailRateLimit.key();
        String intervalKey = keyPrefix + "interval:" + email;
        String hourlyKey = keyPrefix + "hourly:" + email;
        String dailyTotalKey = keyPrefix + "daily_total";
        String blacklistKey = keyPrefix + "blacklist:" + email;

        // 准备参数
        List<String> keys = Arrays.asList(intervalKey, hourlyKey, dailyTotalKey, blacklistKey);
        long currentTimestamp = System.currentTimeMillis() / 1000;

        Object[] argv = {
                emailRateLimit.hourlyLimit(),
                emailRateLimit.interval(),
                emailRateLimit.dailyTotalLimit(),
                currentTimestamp,
                emailRateLimit.blacklistTtl()
        };

        // 执行Lua脚本
        List<Long> result = redisTemplate.execute(
                rateLimitScript,
                keys,
                argv
        );

        if (result == null || result.isEmpty()) {
            throw new BizException("限流系统异常");
        }

        return new RateLimitResult(
                result.get(0),
                result.size() > 1 ? result.get(1) : 0,
                result.size() > 2 ? result.get(2) : 0
        );
    }

    private String getEmailFromArgs(Object[] args) {
        // 根据实际方法参数结构获取邮箱
        for (Object arg : args) {
            if (arg instanceof String) {
                String str = (String) arg;
                // 简单邮箱格式验证
                if (str.contains("@") && str.contains(".")) {
                    return str;
                }
            } else if (arg instanceof EmailRequest) {
                return ((EmailRequest) arg).getEmail();
            }
        }
        return null;
    }

    private String formatTime(long seconds) {
        // 转换时间为友好格式
        if (seconds < 60) return seconds + "秒";
        if (seconds < 3600) return (seconds / 60) + "分钟";
        return (seconds / 3600) + "小时";
    }

    // 限流结果封装类
    @Data
    @AllArgsConstructor
    private static class RateLimitResult {
        private long status;
        private long ttl;
        private long hourlyCount;
        private long dailyTotal;

        public RateLimitResult(long status, long ttl, long hourlyCount) {
            this.status = status;
            this.ttl = ttl;
            this.hourlyCount = hourlyCount;
        }
    }
}
