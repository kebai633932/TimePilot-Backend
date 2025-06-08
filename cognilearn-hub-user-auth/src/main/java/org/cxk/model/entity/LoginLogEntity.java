package org.cxk.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author KJH
 * @description 登录日志
 * @create 2025/6/7 22:47
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginLogEntity {
    private Long id;
    private Long userId;
    private String ip;
    private String userAgent;
    private Integer status;
    private String location;
    private Boolean isDeleted;
    private Date createTime;
}
