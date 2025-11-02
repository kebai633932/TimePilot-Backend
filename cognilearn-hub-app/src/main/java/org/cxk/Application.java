package org.cxk;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Configurable
@EnableRetry
@EnableScheduling
@EnableDubbo
@EnableAspectJAutoProxy(proxyTargetClass = true)
//proxyTargetClass = true : 强制使用 CGLIB 动态代理（而非默认的 JDK 动态代理）
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }
}
