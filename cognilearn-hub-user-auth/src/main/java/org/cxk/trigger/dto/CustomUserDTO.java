package org.cxk.trigger.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
//Spring Security 里的 Jackson 模块会检查：
//
//反序列化的类是否在允许的列表里（白名单）
//
//如果不在列表里，拒绝反序列化，抛出你看到的异常
@Data
@NoArgsConstructor
@AllArgsConstructor
//use = JsonTypeInfo.Id.CLASS：序列化时记录对象的 全限定类名（如 org.cxk.trigger.dto.CustomUserDTO），反序列化时 Jackson 会根据这个类名直接创建对象。
//Jackson 通过这种方式能够 明确知道需要实例化的具体类，无需依赖 Spring Security 的白名单检查（因为类信息是安全明确的）。
//todo ???RPC 需要序列化这个类？？
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class CustomUserDTO  implements UserDetails , Serializable {
    private Long userId;              // 用户ID
    private String username;          // 用户名（用于登录）
    private String password;          // 密码（用于登录）
    private String deviceId;  // 新增设备ID字段
    // 下面4个布尔值控制账号状态，决定用户是否允许登录
    //Jackson 默认根据 getter 方法推断 JSON 字段名
    @JsonProperty("accountNonExpired")
    private Boolean isAccountNonExpired = true;      // 账号是否未过期（true = 有效）
    @JsonProperty("accountNonLocked")
    private Boolean isAccountNonLocked = true;       // 账号是否未被锁定
    @JsonProperty("credentialsNonExpired")
    private Boolean isCredentialsNonExpired = true;  // 凭据（密码）是否未过期
    @JsonProperty("enabled")
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
