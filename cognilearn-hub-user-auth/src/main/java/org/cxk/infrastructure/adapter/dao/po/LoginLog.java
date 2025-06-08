package org.cxk.infrastructure.adapter.dao.po;

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
public class LoginLog {
    private Long id;
    private Long userId;
    private String ip;
    private String userAgent;
    private Integer status;
    private String location;
    private Boolean isDeleted;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
}
