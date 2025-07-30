package org.cxk.trigger.dto;

import lombok.Data;
import lombok.Getter;
import org.cxk.trigger.dto.type.RegisterType;

import java.io.Serializable;

/**
 * @author KJH
 * @description 用户注册，应答对象
 * @create 2025/6/8 8:56
 */
@Data
public class UserRegisterDTO implements Serializable {

    private String username;         // 用户名
    private String password;         // 密码
    private String email;            // 邮箱（邮箱注册用）
    private String phone;            // 手机号（短信注册用）
    private String verificationCode; // 验证码（邮箱或短信验证码）
    private RegisterType registerType; // 注册方式枚举，如 EMAIL, SMS 等
}
