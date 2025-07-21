package org.cxk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
/**
 * @author KJH
 * @description 安全配置中心
 * @create 2025/6/17 21:24
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //todo HTTP的configure方法重写
    // 添加修改过滤器
    //todo 配置异常处理器
    // 跨域看是用分布式网关，还是直接设定


    //CSRF：跨站请求伪造

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        //todo
        return new AuthenticationManager() {

        };
    }

}
