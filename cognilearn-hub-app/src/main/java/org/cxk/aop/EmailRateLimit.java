package org.cxk.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author KJH
 * @description
 * @create 2025/8/2 15:56
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailRateLimit {
    /**
     * 限流key前缀
     */
    String key() default "email:rate:";

    /**
     * 间隔时间（秒） - 相同邮箱发送间隔
     */
    int interval() default 60;

    /**
     * 每小时最大发送次数 - 相同邮箱
     */
    int hourlyLimit() default 10;

    /**
     * 每日系统总量限制 - 所有邮箱
     */
    int dailyTotalLimit() default 500;

    /**
     * 黑名单过期时间（秒）
     */
    int blacklistTtl() default 86400; // 24小时
}
