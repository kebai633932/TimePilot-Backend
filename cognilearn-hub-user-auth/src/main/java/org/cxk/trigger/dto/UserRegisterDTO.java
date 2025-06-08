package org.cxk.trigger.dto;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author KJH
 * @description 用户注册，应答对象
 * @create 2025/6/8 8:56
 */
@Data
public class UserRegisterDTO  implements Serializable {

    // 账号
    private String username;
    // 密码
    private String password;
}
