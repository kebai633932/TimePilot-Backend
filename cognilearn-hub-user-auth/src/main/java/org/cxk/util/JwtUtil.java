package org.cxk.util;

import io.jsonwebtoken.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT工具类 - 非对称加密实现 (RSA算法)
 * 从classpath下的PEM文件读取RSA密钥
 *
 * @author KJH
 */
@Component
@Data
public class JwtUtil {

    // 默认PEM文件路径（可在配置中覆盖）
    @Value("${jwt.rsa.private-key-path:classpath:private_key.pem}")
    private String privateKeyPath;

    @Value("${jwt.rsa.public-key-path:classpath:public_key.pem}")
    private String publicKeyPath;

    // AccessToken有效期（分钟）
    @Value("${jwt.access-token.expiration:10}")
    private int accessTokenExpiration;

    // RefreshToken有效期（天）
    @Value("${jwt.refresh-token.expiration:7}")
    private int refreshTokenExpiration;

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private PrivateKey privateKey;
    private PublicKey publicKey;

    /**
     * 初始化RSA密钥
     */
    @PostConstruct
    public void initKeys() {
        try {
            // 读取PEM文件内容
            String privateKeyPem = readPemFile(privateKeyPath);
            String publicKeyPem = readPemFile(publicKeyPath);

            // 生成密钥对象
            privateKey = generatePrivateKey(privateKeyPem);
            publicKey = generatePublicKey(publicKeyPem);

            System.out.println("✅ RSA密钥加载成功");
        } catch (Exception e) {
            String errorMsg = "❌ RSA密钥初始化失败: " + e.getMessage();
            System.err.println(errorMsg);
            throw new IllegalStateException(errorMsg, e);
        }
    }

    /**
     * 读取PEM文件内容
     */
    private String readPemFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)),
                StandardCharsets.UTF_8);
    }

    /**
     * 清理PEM格式的标记和空白字符
     */
    private String cleanPemKey(String pemContent) {
        return pemContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----END RSA PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
    }

    /**
     * 生成私钥对象
     */
    private PrivateKey generatePrivateKey(String pemContent)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        //密钥数据格式不合法，无法通过密钥规范（KeySpec）转换为密钥对象
        String cleanedKey = cleanPemKey(pemContent);
        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
        //PKCS8
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 生成公钥对象
     */
    private PublicKey generatePublicKey(String pemContent)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        String cleanedKey = cleanPemKey(pemContent);
        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
        //X509
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(Long id, Collection<? extends GrantedAuthority> authorities,String deviceId) {
        return buildToken(id, ACCESS_TOKEN_TYPE, accessTokenExpiration * 60 * 1000L,authorities,deviceId);
    }
    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(Long id, Collection<? extends GrantedAuthority> authorities,String deviceId) {
        return buildToken(id, REFRESH_TOKEN_TYPE, refreshTokenExpiration * 24 * 60 * 60 * 1000L,authorities,deviceId);
    }
    /**
     * 构建JWT令牌
     */
    private String buildToken(Long id, String tokenType, long expiration, Collection<? extends GrantedAuthority> authorities,String deviceId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, tokenType);

        List<String> roleNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roleNames); // 转为字符串列表
        claims.put("deviceId", deviceId);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(id)) // 转为字符串
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }

    /**
     * 验证令牌有效性
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 解析JWT令牌
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从令牌中获取用户ID
     */
    public String getUserIdFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 检查令牌类型
     */
    public boolean isAccessToken(String token) {
        return ACCESS_TOKEN_TYPE.equals(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN_TYPE.equals(getTokenType(token));
    }

    public String getTokenType(String token) {
        return parseToken(token).get(TOKEN_TYPE_CLAIM, String.class);
    }


}