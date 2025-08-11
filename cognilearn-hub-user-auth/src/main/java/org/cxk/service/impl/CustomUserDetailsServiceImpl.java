package org.cxk.service.impl;
import org.cxk.infrastructure.adapter.dao.po.Role;
import org.cxk.infrastructure.adapter.dao.po.User;
import org.cxk.service.CustomUserDetailsService;
import org.cxk.service.repository.IRoleRepository;
import org.cxk.service.repository.IUserRepository;
import org.cxk.trigger.dto.CustomUserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author KJH
 * @description 自定义用户详情服务 - 从数据库加载用户信息
 * @create 2025/7/28 16:03
 */
@Service
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    @Resource
    private IUserRepository userRepository;
    @Resource
    private IRoleRepository roleRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //todo 如果后续登录请求非常频繁，添加redis做缓存
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if(user.getIsDeleted()){
            throw new RuntimeException("账号已停用");
        }
        // 权限信息可以从用户角色表中查
        List<Role> roles = roleRepository.findRolesByUserId(user.getUserId());
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getCode())) // 比如 "ROLE_ADMIN"
                .collect(Collectors.toList());

        // 封装成 CustomUserDTO(实现了UserDetails接口)  返回
        CustomUserDTO userDetails = new CustomUserDTO();
        userDetails.setUserId(user.getUserId());
        userDetails.setUsername(user.getUsername());
        userDetails.setPassword(user.getPassword());
        userDetails.setAuthorities(authorities); // 赋予权限
        userDetails.setDeviceId(null);//
        userDetails.setIsAccountNonExpired(true);
        userDetails.setIsAccountNonLocked(true);
        userDetails.setIsCredentialsNonExpired(true);
        userDetails.setIsEnabled(true);
        return userDetails;
    }
}
