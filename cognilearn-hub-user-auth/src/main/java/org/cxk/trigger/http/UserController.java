package org.cxk.trigger.http;

import cn.hutool.core.util.RandomUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.service.IEmailService;
import org.cxk.service.IUserAuthService;
import org.cxk.trigger.dto.UserDeleteDTO;
import org.cxk.trigger.dto.UserRegisterDTO;
import org.cxk.trigger.dto.type.RegisterType;
import org.cxk.util.JwtUtil;
import org.cxk.util.VerificationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import types.enums.ResponseCode;
import types.response.Response;

import java.util.concurrent.TimeUnit;


/**
 * @author KJH
 * @description
 * @create 2025/5/24 15:27
 */
//todo
@Slf4j
@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {

    private final IUserAuthService userAuthService;
    private final IEmailService emailService;

    @PostMapping("/register")
    public Response<Boolean> register(@RequestBody UserRegisterDTO dto) {
        try {
            // 1. 验证码校验
            if (dto.getRegisterType() == RegisterType.EMAIL) {
                if(!emailService.verifyEmailCode(dto.getEmail(), dto.getVerificationCode()))
                    return Response.error(ResponseCode.VERIFICATION_CODE_ERROR);
            }
//            else if (dto.getRegisterType() == RegisterType.SMS) {
//                verificationService.verifySmsCode(dto.getPhone(), dto.getVerificationCode());
//            }
            else {
                return Response.error(ResponseCode.ILLEGAL_PARAMETER, "未知的注册类型");
            }

            // 2. 调用注册服务
            boolean success = userAuthService.register(dto);
            return success
                    ? Response.success(true)
                    : Response.error(ResponseCode.UN_ERROR);
        } catch (Exception e) {
            // 注意避免打印密码
            log.error("注册失败，用户名：{}，邮箱：{}，手机号：{}", dto.getUsername(), dto.getEmail(), dto.getPhone(), e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("注册出现异常：" + e.getMessage())
                    .build();
        }
    }
    // 你也可以添加发送验证码接口
    @PostMapping("/sendEmailCode")
    public Response<Boolean> sendEmailCode(@RequestParam String email) {
        // 1.校验邮箱格式是否有效
        if (!VerificationUtil.isValidEmail(email)) {
            return Response.error(ResponseCode.ILLEGAL_PARAMETER, "邮箱格式不正确");
        }

        try {
            // 2.发送验证码邮件
            emailService.sendVerificationEmail(email);

            return Response.success(true);
        } catch (Exception e) {
            log.error("发送邮箱验证码失败，邮箱：{}", email, e);
            return Response.error(ResponseCode.UN_ERROR, "发送验证码失败");
        }
    }


    @PostMapping("/delete")
    public Response<Boolean> delete(@RequestBody UserDeleteDTO dto) {
        try {
            boolean success = userAuthService.delete(dto);
            return success
                    ? Response.success(true)
                    : Response.error(ResponseCode.UN_ERROR);
        } catch (Exception e) {
            log.error("删除失败，参数：{}", dto.getUsername(), e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("删除出现异常：" + e.getMessage())
                    .build();
        }
    }

}
