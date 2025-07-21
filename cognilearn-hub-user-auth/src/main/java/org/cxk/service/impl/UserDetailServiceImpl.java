package org.cxk.service.impl;

import org.cxk.infrastructure.adapter.dao.po.User;
import org.cxk.infrastructure.adapter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author KJH
 * @description
 * @create 2025/7/16 22:31
 */
@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //todo
        //查询用户信息
        //如果没有查询到用户就抛出异常
        //todo 查询对应的权限信息
        //把数据封装成UserDetail返回
        //todo LoginUser implements UserDetail
        //LoginUser 有user，permissions,authorities(不序列化) List<GrantedAuthority>
        return null;
    }
}
