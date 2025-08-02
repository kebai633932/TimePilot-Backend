package org.cxk.util;
import java.util.regex.Pattern;
/**
 * @author KJH
 * @description 验证工具类，提供邮箱、手机号、用户名、密码等格式校验方法
 * @create 2025/7/31 9:17
 */
public class VerificationUtil {


    // 手机号正则：以中国大陆手机号为例（以1开头 + 10位数字）
    private static final Pattern SMS_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    // 用户名正则：字母开头，允许字母数字下划线，长度3-16位
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{2,15}$");

    // 密码正则：必须包含大小写字母和数字，长度8-20位，允许特殊符号
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@#$%^&+=!~*()_\\-]{8,20}$"
    );

    /**
     * 校验手机号格式（国内手机号）
     */
    public static boolean isValidSms(String phone) {
        return phone != null && SMS_PATTERN.matcher(phone).matches();
    }

    /**
     * 校验用户名格式
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * 校验密码复杂度
     */
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}

