package org.cxk.trigger.dto;

import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author KJH
 * @description 自定义用户信息 DTO，用于 Spring Security 登录流程
 * @create 2025/7/29 8:53
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDTO  implements UserDetails , Serializable {
    private Long userId;              // 用户ID
    private String username;          // 用户名（用于登录）
    private String password;          // 密码（用于登录）
    private String deviceId;  // 新增设备ID字段
    // 下面4个布尔值控制账号状态，决定用户是否允许登录
    private Boolean isAccountNonExpired = true;      // 账号是否未过期（true = 有效）
    private Boolean isAccountNonLocked = true;       // 账号是否未被锁定
    private Boolean isCredentialsNonExpired = true;  // 凭据（密码）是否未过期
    private Boolean isEnabled = true;                // 是否启用账号（false = 禁用）

    // 用户的权限集合（可以为空）
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDTO(Long userId, String username, String password, String deviceId,Collection<? extends GrantedAuthority> authorities) {
        this.userId=userId;
        this.username=username;
        this.password=password;
        this.deviceId=deviceId;
        this.authorities=authorities;
        this.isAccountNonExpired = true;
        this.isAccountNonLocked = true;
        this.isCredentialsNonExpired = true;
        this.isEnabled = true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return Boolean.TRUE.equals(isAccountNonExpired);
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(isAccountNonLocked);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return Boolean.TRUE.equals(isCredentialsNonExpired);
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isEnabled);
    }
}
