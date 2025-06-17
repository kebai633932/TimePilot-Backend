package org.cxk.trigger.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author KJH
 * @description
 * @create 2025/6/17 21:10
 */
@Data
public class UserLoginDTO   implements Serializable {

    // 账号
    private String username;
    // 密码
    private String password;
}
