package org.cxk.infrastructure.adapter.dao.po;

import lombok.Data;

import java.util.Date;
import java.util.Set;

/**
 * @author KJH
 * @description 用户实体
 * @create 2025/6/7 21:45
 */
@Data
public class User {
    private Long id;
    private String username;
    private String realName;
    private String password;
    private String email;
    private String phone;
    private Boolean isDeleted;
    private Date lastLoginTime;
    private Set<Role> roles;//todo  建议角色延迟加载或另建表
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
    // 构造函数、getter/setter、equals/hashCode
}
