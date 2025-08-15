package types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举，包含通用及业务错误码
 *
 * @author KJH
 * @create 2025/6/8 9:19
 */
@AllArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS("0000", "调用成功"),
    UN_ERROR("0001", "调用失败"),
    ILLEGAL_PARAMETER("0002", "非法参数"),
    INDEX_DUP("0003", "唯一索引冲突"),
    DEGRADE_SWITCH("0004", "活动已降级"),
    RATE_LIMITER("0005", "访问限流拦截"),
    HYSTRIX("0006", "访问熔断拦截"),

    // 业务错误码，建议以 ERR_BIZ_ 开头
    VERIFICATION_CODE_ERROR("ERR_BIZ_001", "验证码不匹配,输入错误"),
    UN_AUTHORIZED("ERR_BIZ_002","无权限修改该文件夹" ),

    // 配置错误码,以ERR_CONFIG_

    // 中间件相关错误码 ERR_MIDDLE_
    REDIS_UNAVAILABLE("ERR_MIDDLE_001", "Redis 不可用"),
    MYSQL_CONNECTION_ERROR("ERR_MIDDLE_002", "数据库连接失败"),
    MQ_SEND_FAILED("ERR_MIDDLE_003", "消息发送失败"),
    LOCAL_CACHE_FAIL("ERR_MIDDLE_004", "本地缓存读取失败"),
    // 服务类错误码 ERR_SERVICE_
    EMAIL_SEND_ERROR("ERR_SERVICE_001", "邮件发送失败");




    private final String code;
    private final String info;

    @Override
    public String toString() {
        return code + ": " + info;
    }
}
