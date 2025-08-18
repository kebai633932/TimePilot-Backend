package org.cxk.domain;

import org.cxk.trigger.dto.*;

/**
 * @author KJH
 * @description
 * @create 2025/4/25 1:00
 */
public interface IUserAuthService {
    boolean register(UserRegisterDTO userRegisterDTO);
    boolean delete(UserDeleteDTO userDeleteDTO);

    // 忘记密码用，校验验证码 + 设置新密码
    boolean resetPassword(ForgotPasswordResetDTO dto);

    // 登录后修改密码，需要验证旧密码
    boolean resetPasswordAfterLogin(String username, ResetPasswordDTO dto);

}
