package org.cxk.trigger.http;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.service.IUserAuthService;
import org.cxk.trigger.dto.UserLoginDTO;
import org.cxk.trigger.dto.UserRegisterDTO;
import org.springframework.web.bind.annotation.*;
import types.enums.ResponseCode;
import types.response.Response;


/**
 * @author KJH
 * @description
 * @create 2025/5/24 15:27
 */
@Slf4j
@RestController
@RequestMapping("/api/user/auth")
@AllArgsConstructor
public class UserAuthController {

    private final IUserAuthService userAuthService;


    @PostMapping("/register")
    public Response<Boolean> register(@RequestBody UserRegisterDTO dto) {
        try {
            boolean success = userAuthService.register(dto);
            return success
                    ? Response.success(true)
                    : Response.error(ResponseCode.UN_ERROR);
        } catch (Exception e) {
            log.error("注册失败，参数：{}", dto, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("注册出现异常：" + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/login")
    public Response<Boolean> login(@RequestBody UserLoginDTO dto) {
        try {
            boolean success = userAuthService.login(dto);
            return success
                    ? Response.success(true)
                    : Response.error(ResponseCode.UN_ERROR);
        } catch (Exception e) {
            log.error("登录失败，参数：{}", dto, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("登录出现异常：" + e.getMessage())
                    .build();
        }
    }


}
