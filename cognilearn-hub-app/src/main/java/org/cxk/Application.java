package org.cxk;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@Configurable
@EnableRetry
@EnableAspectJAutoProxy(proxyTargetClass = true)
//proxyTargetClass = true : 强制使用 CGLIB 动态代理（而非默认的 JDK 动态代理）
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }
}
