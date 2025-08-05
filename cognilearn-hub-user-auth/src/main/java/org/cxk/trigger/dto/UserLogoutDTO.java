package org.cxk.trigger.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author KJH
 * @description
 * @create 2025/8/4 14:39
 */
@Data
public class UserLogoutDTO implements Serializable {

    private String refreshToken;
}
