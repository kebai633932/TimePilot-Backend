package org.cxk.config;

import org.cxk.handler.CustomMetaObjectHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author KJH
 * @description
 * @create 2025/7/29 9:21
 */
@Configuration
@MapperScan("org.cxk.infrastructure.adapter.dao") // Mapper 扫描路径
public class MyBatisPlusConfig {

    @Bean
    public CustomMetaObjectHandler myMetaObjectHandler() {
        return new CustomMetaObjectHandler();
    }
}
