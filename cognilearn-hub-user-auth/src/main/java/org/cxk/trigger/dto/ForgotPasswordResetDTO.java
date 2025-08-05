package org.cxk.trigger.dto;

import lombok.Data;
import org.cxk.trigger.dto.type.VerificationChannelType;

import java.io.Serializable;

/**
 * @author KJH
 * @description
 * @create 2025/8/3 22:41
 */
@Data
public class ForgotPasswordResetDTO implements Serializable {
    private String username;
    private String password;
    private String email;
    private String verificationCode; // 验证码（邮箱或短信验证码）
    private VerificationChannelType verificationChannelType; // 注册方式枚举，如 EMAIL, SMS 等
}
