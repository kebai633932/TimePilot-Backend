package org.cxk.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

/**
 * @author KJH
 * @description 用户实体
 * @create 2025/6/7 21:45
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    private Long id;
    private Long userId;
    private String username;
    private String password;
    private String email;
    private String phone;
    private Boolean isDeleted;
    private Date lastLoginTime;
    private Set<RoleEntity> roles;
    private Long delVersion;

    // 构造函数、getter/setter、equals/hashCode
}
