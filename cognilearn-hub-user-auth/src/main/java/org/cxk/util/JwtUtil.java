package org.cxk.util;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
/**
 * @author KJH
 * @description
 * @create 2025/6/7 21:46
 */
//todo 完善，jwt的token创建，解析
// 有hutoo的jwt工具类
@Component
public class JwtUtil {

    private static final String SECRET = "your_secret";
    private static final long ACCESS_TOKEN_EXP = 10 * 60 * 1000;  // 10分钟
    private static final long REFRESH_TOKEN_EXP = 7 * 24 * 60 * 60 * 1000;  // 7天

    public String createToken(String userId, boolean isRefresh) {
        long exp = isRefresh ? REFRESH_TOKEN_EXP : ACCESS_TOKEN_EXP;
        return Jwts.builder()
                .setSubject(userId)
                .claim("refresh", isRefresh)
                .setExpiration(new Date(System.currentTimeMillis() + exp))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }
}

