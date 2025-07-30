package org.cxk.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author KJH
 * @description
 * @create 2025/7/29 8:39
 */
//todo
public interface CustomUserDetailsService extends org.springframework.security.core.userdetails.UserDetailsService{
    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
