package org.cxk.util;

import io.jsonwebtoken.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import types.exception.BizException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@Component
public class JwtUtil {

    // 配置属性
    @Value("${jwt.rsa.private-key-path:classpath:private_key.pem}")
    private String privateKeyPath;

    @Value("${jwt.rsa.public-key-path:classpath:public_key.pem}")
    private String publicKeyPath;

    @Value("${jwt.access-token.expiration:10}")
    private int accessTokenExpirationMinutes;

    @Value("${jwt.refresh-token.expiration:7}")
    private int refreshTokenExpirationDays;

    // 常量定义
    public static final String TOKEN_TYPE_CLAIM = "token_type";
    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";
    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String JWT_FORMAT_REGEX = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

    // Redis键前缀
    private static final String REDIS_JWT_BLACKLIST_PREFIX = "jwt:jwi:blacklist:";
    private static final String REDIS_JTI_PREFIX = "jwt:refresh_jti:";
    private static final String REDIS_LOCK_PREFIX = "jwt:lock:refresh:";

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Resource
    private RedissonClient redissonClient;

    @PostConstruct
    public void initKeys() {
        try {
            privateKey = loadPrivateKey();
            publicKey = loadPublicKey();
            log.info("✅ RSA密钥初始化成功");
        } catch (Exception e) {
            log.error("❌ RSA密钥初始化失败", e);
            throw new IllegalStateException("JWT密钥加载失败，应用无法启动", e);
        }
    }

    private PrivateKey loadPrivateKey() throws Exception {
        String privateKeyPem = readKeyFromClasspath(privateKeyPath);
        return parsePrivateKey(privateKeyPem);
    }

    private PublicKey loadPublicKey() throws Exception {
        String publicKeyPem = readKeyFromClasspath(publicKeyPath);
        return parsePublicKey(publicKeyPem);
    }

    private String readKeyFromClasspath(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
        }
        ClassPathResource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    private PrivateKey parsePrivateKey(String pemContent) throws Exception {
        String cleanedKey = cleanPemKey(pemContent, "PRIVATE");
        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    private PublicKey parsePublicKey(String pemContent) throws Exception {
        String cleanedKey = cleanPemKey(pemContent, "PUBLIC");
        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    private String cleanPemKey(String pemContent, String keyType) {
        return pemContent
                .replaceAll("-----BEGIN " + keyType + " KEY-----", "")
                .replaceAll("-----END " + keyType + " KEY-----", "")
                .replaceAll("\\s", "");
    }

    // ====================== 令牌生成方法 ======================
    public String generateAccessToken(String username, List<String> roles, String deviceId, Long userId) {
        return buildToken(username, ACCESS_TOKEN_TYPE, UUID.randomUUID().toString(),
                TimeUnit.MINUTES.toMillis(accessTokenExpirationMinutes),
                roles, deviceId, userId);
    }

    public String generateRefreshToken(String username, String jti, List<String> roles, String deviceId, Long userId) {
        return buildToken(username, REFRESH_TOKEN_TYPE, jti,
                TimeUnit.DAYS.toMillis(refreshTokenExpirationDays),
                roles, deviceId, userId);
    }

    private String buildToken(String subject, String tokenType, String jti, long expirationMs,
                              List<String> roles, String deviceId, Long userId) {

        Claims claims = Jwts.claims()
                .setSubject(subject)
                .setId(jti)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs));

        claims.put(TOKEN_TYPE_CLAIM, tokenType);
        claims.put("roles", roles);
        claims.put("deviceId", deviceId);
        claims.put("uid", userId);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }

    // ====================== 令牌解析方法 ======================
    //RFC 6750 规范：要求 Bearer 大小写不敏感，parseJwt() 更符合标准。
    public String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(AUTH_HEADER);
        if (!StringUtils.hasText(headerAuth)) return null;

        // 统一转为小写比较，避免额外分支
        String prefix = headerAuth.substring(0, 7).toLowerCase();
        if (prefix.equals("bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    public Claims parseToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(token)
                .getBody();
        return claims;

    }

    public Claims safeParseToken(String token) {
        try {
            return parseToken(token);
        } catch (ExpiredJwtException e) {
            throw new BizException("令牌已过期");
        } catch (JwtException | IllegalArgumentException e) {
            throw new BizException("无效令牌: " + e.getMessage());
        }
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("令牌验证失败: {} - 原因: {}", token, e.getMessage());
            return false;
        }
    }

    public void validateRefreshTokenClaims(Claims claims) {
        if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new BizException("非法的Refresh Token类型");
        }

        if (claims.get("uid", Long.class) == null ||
                claims.get("deviceId", String.class) == null) {
            throw new BizException("缺失必要的Token声明");
        }
    }

    // ====================== 黑名单管理 ======================
    public void safeRevokeToken(String token) {
        if (!StringUtils.hasText(token)) return;

        try {
            // 尝试解析令牌
            Claims claims = parseToken(token);
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttl > 0) addToBlacklist(claims, ttl);
        } catch (ExpiredJwtException e) {
            // 过期令牌加入黑名单30秒，以防刚过期就被重放攻击
            addToBlacklist(e.getClaims(), 30000);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("吊销无效令牌失败: {} - 原因: {}", token, e.getMessage());
            // 格式错误令牌短期黑名单，
            // 防止恶意攻击者尝试伪造 Token 刷接口，每次都消耗 CPU 做签名验证。用 Redis 缓存拦下来，减少资源浪费
            RBucket<String> bucket = redissonClient.getBucket("jwt:invalid:" + token);
            bucket.set("revoked", 1, TimeUnit.MINUTES);
        }
    }

    private void addToBlacklist(Claims claims, long ttlMillis) {
        String jti = claims.getId();
        RBucket<String> bucket = redissonClient.getBucket(REDIS_JWT_BLACKLIST_PREFIX + jti);
        bucket.set("revoked", ttlMillis, TimeUnit.MILLISECONDS);
        log.debug("令牌加入黑名单: jti={}, ttl={}ms", jti, ttlMillis);
    }

    public boolean isTokenBlacklisted(Claims claims) {
        String jti = claims.getId();
        return redissonClient.getBucket(REDIS_JWT_BLACKLIST_PREFIX + jti).isExists();
    }

    // ====================== JTI管理 ======================
    public void saveRefreshTokenJti(Long userId, String deviceId, String jti) {
        try {
            String key = getJtiKey(userId, deviceId);
            long ttl = TimeUnit.DAYS.toMillis(refreshTokenExpirationDays);
            redissonClient.getBucket(key).set(jti, ttl, TimeUnit.MILLISECONDS);
            log.debug("保存刷新令牌JTI: userId={}, deviceId={}, jti={}", userId, deviceId, jti);
        } catch (Exception e) {
            log.error("保存刷新令牌JTI失败", e);
            throw new BizException("令牌保存失败");
        }
    }

    public boolean isJtiValid(Long userId, String deviceId, String jti) {
        if (userId == null || deviceId == null || jti == null) {
            log.warn("验证JTI失败: 参数为空");
            return false;
        }

        try {
            String key = getJtiKey(userId, deviceId);
            RBucket<String> bucket = redissonClient.getBucket(key);
            String storedJti = bucket.get();
            return jti.equals(storedJti);
        } catch (Exception e) {
            log.error("验证JTI失败", e);
            return false;
        }
    }

    private String getJtiKey(Long userId, String deviceId) {
        return REDIS_JTI_PREFIX + userId + ":" + deviceId;
    }

    public void clearDeviceJti(Long userId, String deviceId) {
        if (userId == null || deviceId == null) return;

        try {
            String key = getJtiKey(userId, deviceId);
            boolean deleted = redissonClient.getBucket(key).delete();
            log.debug("清理设备JTI: userId={}, deviceId={}, 结果={}", userId, deviceId, deleted ? "成功" : "失败");
        } catch (Exception e) {
            log.error("清理设备JTI失败", e);
        }
    }

    public String getRefreshTokenJti(Long userId, String deviceId) {
        if (userId == null || deviceId == null) return null;

        try {
            String key = getJtiKey(userId, deviceId);
            RBucket<String> bucket = redissonClient.getBucket(key);
            return bucket.get();
        } catch (Exception e) {
            log.error("获取刷新令牌JTI失败", e);
            return null;
        }
    }
    // ====================== 辅助方法 ======================
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = safeParseToken(token);
            return claims.get("uid", Long.class);
        } catch (BizException e) {
            log.warn("从令牌获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    public String getDeviceIdFromToken(String token) {
        try {
            Claims claims = safeParseToken(token);
            return claims.get("deviceId", String.class);
        } catch (BizException e) {
            log.warn("从令牌获取设备ID失败: {}", e.getMessage());
            return null;
        }
    }


}