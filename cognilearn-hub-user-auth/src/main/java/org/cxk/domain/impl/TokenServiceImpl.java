package org.cxk.domain.impl;

import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.cxk.domain.ITokenService;
import org.cxk.trigger.dto.TokenPairResponseDTO;
import org.cxk.util.JwtUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.cxk.types.exception.BizException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
//todo ddd解耦
public class TokenServiceImpl implements ITokenService {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private RedissonClient redissonClient;

    @Value("${jwt.refreshLock.waitTime:1}")
    private long lockWaitTime;

    @Value("${jwt.refreshLock.leaseTime:5}")
    private long lockLeaseTime;

    @Override
    public TokenPairResponseDTO refreshToken(String refreshToken, String currentAccessToken) {
        // 1. 安全解析刷新令牌
        Claims refreshClaims = jwtUtil.safeParseTokenStrict(refreshToken);
        jwtUtil.validateRefreshTokenClaims(refreshClaims);

        // 2. 解析当前访问令牌（用于设备ID和用户ID），这里不需要抛出ExpiredJwtException
        Claims accessClaims = jwtUtil.safeParseTokenAllowExpired(currentAccessToken);

        // 3. 验证设备绑定和用户一致性
        validateTokenConsistency(refreshClaims, accessClaims);

        // 4. 获取分布式锁（用户ID+设备ID）
        Long userId = refreshClaims.get("userId", Long.class);
        String deviceId = refreshClaims.get("deviceId", String.class);
        String lockKey = "refresh_lock:" + userId + ":" + deviceId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 5. 尝试获取锁
            if (!lock.tryLock(lockWaitTime, lockLeaseTime, TimeUnit.SECONDS)) {
                throw new BizException("系统繁忙，请稍后重试");
            }

            // 6. 检查刷新令牌状态
            if (jwtUtil.isTokenBlacklisted(refreshClaims)) {
                throw new BizException("刷新令牌已失效");
            }

            // 7. 检查当前刷新令牌是否匹配设备绑定
            String currentDeviceJti = jwtUtil.getRefreshTokenJti(userId, deviceId);
            if (!refreshClaims.getId().equals(currentDeviceJti)) {
                log.warn("刷新令牌不匹配: userId={}, deviceId={}", userId, deviceId);
                throw new BizException("刷新令牌已失效（设备不匹配）");
            }

            // 8. 吊销旧令牌（先吊销刷新令牌再访问令牌）
            jwtUtil.safeRevokeToken(refreshToken);
            jwtUtil.safeRevokeToken(currentAccessToken);

            // 9. 生成新令牌
            String newJti = UUID.randomUUID().toString();
            String username = refreshClaims.getSubject();

            List<String> roleList = extractRoles(refreshClaims);

            // 10. 创建新访问令牌
            String newAccessToken = jwtUtil.generateAccessToken(
                    username,
                    roleList,
                    deviceId,
                    userId
            );

            // 11. 创建新刷新令牌
            String newRefreshToken = jwtUtil.generateRefreshToken(
                    username,
                    newJti,
                    roleList,
                    deviceId,
                    userId
            );

            // 12. 保存新JTI并记录审计日志
            jwtUtil.saveRefreshTokenJti(userId, deviceId, newJti);
            log.info("令牌刷新成功: userId={}, deviceId={}, newJti={}", userId, deviceId, newJti);

            return new TokenPairResponseDTO(newAccessToken, newRefreshToken);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("操作被中断");
        } finally {
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        try {
            // 1. 安全解析令牌
            Claims accessClaims = jwtUtil.safeParseTokenStrict(accessToken);
            Claims refreshClaims = jwtUtil.safeParseTokenStrict(refreshToken);

            // 2. 验证令牌一致性
            validateTokenConsistency(accessClaims, refreshClaims);

            // 3. 获取用户和设备信息
            Long userId = accessClaims.get("userid", Long.class);
            String deviceId = accessClaims.get("deviceId", String.class);

            // 4. 安全吊销令牌
            jwtUtil.safeRevokeToken(accessToken);
            jwtUtil.safeRevokeToken(refreshToken);

            // 5. 清理设备相关的刷新令牌JTI
            jwtUtil.clearDeviceJti(userId, deviceId);

            log.info("用户登出成功: userId={}, deviceId={}", userId, deviceId);
        } catch (BizException e) {
            log.warn("安全登出警告: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("登出过程中发生未预期错误", e);
            throw new BizException("登出异常：" + e.getMessage());
        }
    }

    /**
     * 验证两个令牌的一致性（用户ID和设备ID）
     * //TODO 硬编程怎么写
     */
    private void validateTokenConsistency(Claims claims1, Claims claims2) {
        Long uid1 = claims1.get("userId", Long.class);
        Long uid2 = claims2.get("userId", Long.class);
        String deviceId1 = claims1.get("deviceId", String.class);
        String deviceId2 = claims2.get("deviceId", String.class);

        if (uid1 == null || !uid1.equals(uid2)) {
            log.error("令牌用户ID不匹配: {} vs {}", uid1, uid2);
            throw new BizException("令牌用户不匹配");
        }

        if (deviceId1 == null || !deviceId1.equals(deviceId2)) {
            log.error("令牌设备ID不匹配: {} vs {}", deviceId1, deviceId2);
            throw new BizException("令牌设备不匹配");
        }
    }
    private List<String> extractRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List<?>) {
            return ((List<?>) roles).stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        } else {
            throw new BizException("用户权限roles格式错误");
        }
    }
}