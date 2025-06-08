package org.cxk.trigger.http;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.cxk.service.IUserAuthService;
import org.cxk.trigger.dto.UserRegisterDTO;
import org.springframework.web.bind.annotation.*;
import types.enums.ResponseCode;
import types.response.Response;


/**
 * @author KJH
 * @description
 * @create 2025/5/24 15:27
 */

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class UserAuthController {

    private final IUserAuthService userAuthService;


    @PostMapping("/register")
    public Response<Boolean> register(
            //todo @Valid 可以新增
            @RequestBody  UserRegisterDTO dto) {
        boolean success = userAuthService.register(dto);
        return success   //todo 把异常往外抛，写一个全局异常处理
                ? Response.success(true)
                : Response.error(ResponseCode.UN_ERROR);
    }

}
