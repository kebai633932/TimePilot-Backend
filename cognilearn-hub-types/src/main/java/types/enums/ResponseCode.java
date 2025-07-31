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
    VERIFICATION_CODE_ERROR("ERR_BIZ_VERIFICATION_CODE", "验证码错误"),

    // 配置错误码,以ERR_CONFIG_
    ;

    private final String code;
    private final String info;

    @Override
    public String toString() {
        return code + ": " + info;
    }
}
