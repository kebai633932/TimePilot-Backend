package org.cxk.trigger.http;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.service.IUserAuthService;
import org.cxk.trigger.dto.UserDeleteDTO;
import org.cxk.trigger.dto.UserLoginDTO;
import org.cxk.trigger.dto.UserRegisterDTO;
import org.cxk.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import types.enums.ResponseCode;
import types.response.Response;


/**
 * @author KJH
 * @description
 * @create 2025/5/24 15:27
 */
//todo
@Slf4j
@RestController
@RequestMapping("/api/user/auth")
@AllArgsConstructor
public class UserAuthController {

    private final IUserAuthService userAuthService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

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
