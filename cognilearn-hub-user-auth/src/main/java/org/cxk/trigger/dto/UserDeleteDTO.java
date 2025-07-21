package org.cxk.trigger.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author KJH
 * @description
 * @create 2025/6/18 15:37
 */
@Data
public class UserDeleteDTO  implements Serializable {

    // 账号
    private String username;
}