package org.cxk.trigger.http;


import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.cxk.api.response.Response;
import org.cxk.domain.IEmailService;
import org.cxk.domain.ITokenService;
import org.cxk.domain.IUserAuthService;
import org.cxk.trigger.dto.*;
import org.cxk.trigger.dto.type.VerificationChannelType;
import org.cxk.types.enums.ResponseCode;
import org.cxk.types.exception.BizException;
import org.cxk.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

/**
 * @author KJH
 * @description
 * @create 2025/5/24 15:27
 */
@Slf4j
@RestController
@RequestMapping("/api/user/auth")
@AllArgsConstructor
//todo ddd解耦
public class UserAuthController {

    private final IUserAuthService userAuthService;
    //todo 以后的邮箱，手机号等等，map<String,Bean>自动注入
    private final IEmailService emailService;
    private final JwtUtil jwtUtil;

    private final ITokenService tokenService;
    @PostMapping("/register")
    public Response<Boolean> register(@RequestBody UserRegisterDTO dto) {
        try {
            // 1. 验证码校验
            if (dto.getVerificationChannelType() == VerificationChannelType.EMAIL) {
                if(!emailService.verifyEmailCode(dto.getEmail(), dto.getVerificationCode()))
                    return Response.error(ResponseCode.VERIFICATION_CODE_ERROR);
            }
            else {
                return Response.error(ResponseCode.ILLEGAL_PARAMETER, "未知的注册类型");
            }

            // 2. 调用注册服务
            boolean success = userAuthService.register(dto);
            return success
                    ? Response.success(true, "注册成功")
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


    // 2. 提交验证码 ，完成密码重置 用3
    // todo 1.同一邮箱的账号全部更改为新密码  2.在输入了邮箱后就返回邮箱对应的用户列表，用户需选择修改的用户
    // todo 3.账号名+邮箱双重验证，忘记用户名用2 / 引导用户“重新注册” / “联系客服人工找回”
    @PostMapping("/forgot-password/reset")
    public Response<Boolean> resetPassword(@RequestBody ForgotPasswordResetDTO dto) {
        try {
            // 1. 验证码校验
            if (dto.getVerificationChannelType() == VerificationChannelType.EMAIL) {
                if(!emailService.verifyEmailCode(dto.getEmail(), dto.getVerificationCode()))
                    return Response.error(ResponseCode.VERIFICATION_CODE_ERROR);
            } else {
                return Response.error(ResponseCode.ILLEGAL_PARAMETER, "未知的注册类型");
            }
            // 3. 调用密码重置服务
            boolean success = userAuthService.resetPassword(dto);
            return success
                    ? Response.success(true, "注册成功")
                    : Response.error(ResponseCode.UN_ERROR);
        } catch (Exception e) {
            // 注意避免打印密码
            log.error("密码重置失败，邮箱：{}", dto.getEmail());
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("密码重置出现异常：" + e.getMessage())
                    .build();
        }
    }
    //Refresh Token 轮换 + 黑名单吊销机制,容易缓存爆炸
    //单点登录 + JTI（JWT ID）
    //每个用户在任意时刻只拥有一个有效 Refresh Token，防止被复制、复用、重放攻击，节省 Redis 内存，无需维护黑名单。
    //jti 一般是 UUID，只有几十字节，而完整 Refresh Token JWT 通常超过 200 字节，存储更轻量。
    @PostMapping("/refresh-token")
    public Response<TokenPairResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO dto, HttpServletRequest request) {

        try {
            // 从Header中提取访问令牌
            String currentAccessToken = jwtUtil.parseJwt(request);

            TokenPairResponseDTO tokenPair = tokenService.refreshToken(
                    dto.getRefreshToken(),
                    currentAccessToken
            );

            return Response.success(tokenPair, "刷新成功");
        } catch (BizException e) {
            log.error("刷新令牌异常："+e.getMessage());
            return Response.error(ResponseCode.UN_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("刷新异常", e);
            return Response.error(ResponseCode.UN_ERROR, "系统异常");
        }
    }
    //退出，把刷新令牌与访问令牌的JTI放入黑名单
    @PostMapping("/logout")
    public Response<Boolean> logout(@RequestBody UserLogoutDTO dto, HttpServletRequest request) {
        try {
            // 1. 安全地解析访问令牌
            String accessToken = jwtUtil.parseJwt(request);
            String refreshToken = dto.getRefreshToken();

            tokenService.logout(accessToken, refreshToken);

            return Response.success(true, "登出成功");
        } catch (Exception e) {
            log.error("登出过程中发生未预期错误", e);
            return Response.error(ResponseCode.UN_ERROR, "登出异常：" + e.getMessage());
        }
    }

    //todo  登录后的重置密码
//    @PostMapping("/reset-password")
//    public Response<Boolean> resetLoggedInPassword(@RequestBody ResetPasswordDTO dto, Principal principal) {
//    }

    // 发送邮箱验证码接口
    @PostMapping("/sendEmailCode")
    public Response<Boolean> sendEmailCode(@RequestParam String email) {
        // 1.校验邮箱格式是否有效
        if (!EmailValidator.getInstance().isValid(email)) {
            return Response.error(ResponseCode.ILLEGAL_PARAMETER, "邮箱格式不正确");
        }

        try {
            // 2.发送验证码邮件
            emailService.sendVerificationEmail(email);
            //用了异步后，这里只是服务器发送验证码到SMTP成功，后续不知道
            return Response.success(true,"发送验证码成功");
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
                    ? Response.success(true, "发送验证码成功")
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
