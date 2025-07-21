package org.cxk.service.expression;

import org.springframework.stereotype.Component;

/**
 * @author KJH
 * @description
 * @create 2025/7/18 14:36
 */
@Component("")//添加名字
public class SecurityExpressionRoot {
    //再在preauthorize中@对应名字
    public boolean hasAuthority(String authority){
        //通过securityContextHolder 获取用户当前权限
        //判断用户权限集合中是否存在authority
    }
}
