package org.cxk.service.impl;
import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.infrastructure.adapter.dao.po.User;

import org.cxk.model.entity.UserEntity;
import org.cxk.service.IUserAuthService;
import org.cxk.service.repository.IUserRepository;
import org.cxk.service.repository.IUserRoleRepository;
import org.cxk.trigger.dto.ForgotPasswordResetDTO;
import org.cxk.trigger.dto.ResetPasswordDTO;
import org.cxk.trigger.dto.UserDeleteDTO;
import org.cxk.trigger.dto.UserRegisterDTO;

import org.cxk.trigger.filter.RedisUserBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisTimeoutException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import types.exception.BizException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author KJH
 * @description 注册登录服务接口
 * @create 2025/4/25 0:47
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements IUserAuthService {

    private final IUserRepository userRepository;
    private final IUserRoleRepository userRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final RedissonClient redissonClient;

    private final TransactionTemplate transactionTemplate;
    private final RedisUserBloomFilter redisUserBloomFilter;
    @Override
    public boolean register(UserRegisterDTO dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();
        //todo redis key格式放一起
        String lockKey = "lock:register:" + username;
        RLock lock = redissonClient.getLock(lockKey);

        boolean redisAvailable = true;
        boolean locked = false;

        try {
            // 尝试获取分布式锁
            try {
                //  建议参数化或根据实际并发量、服务延迟评估
                locked = lock.tryLock(1, 5, TimeUnit.SECONDS);
            } catch (RedisConnectionException | RedisTimeoutException | IllegalStateException e) {
                redisAvailable = false;
                log.warn("Redis 不可用，注册流程将跳过分布式锁（已开启唯一索引兜底）", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("线程中断，注册失败", e);
            }

            // Redis 可用但未成功获取锁
            if (redisAvailable && !locked) {
                throw new RuntimeException("注册请求过多或系统繁忙，请稍后再试");
            }
            //todo 后续如果数据库瞬时压力爆炸，做限流
            //注册逻辑
            Long userId = TinyId.nextId("auth_register");

            UserEntity user = UserEntity.builder()
                    .userId(userId)
                    .username(username)
                    .phone(dto.getPhone())
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(password))
                    .isDeleted(false)
                    .delVersion(0L)
                    .build();

            // 事务执行
            // 默认角色：普通用户
            transactionTemplate.execute(status -> {
                try {
                    userRepository.save(user);

                    // 默认角色：普通用户
                    List<Long> roleIds = Collections.singletonList(2L);
                    userRoleRepository.insertUserRoles(userId, roleIds);

                    return true; // 成功
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    throw new BizException("用户名已存在");
                } catch (Exception e) {
                    status.setRollbackOnly();
                    throw e;
                }
            });
            //执行布隆过滤器插入
            try {
                redisUserBloomFilter.add(username);
            } catch (Exception e) {
                log.error("布隆过滤器写入异常，不影响注册流程", e);
                //TODO: 异步重试，发送MQ，异步通知消费者去重试写入布隆过滤器
                // 消费者重试若多次失败，再写入数据库补偿表（task表）以便后续人工或定时任务处理
            }
            return true;
        } finally {
            // 仅在锁成功时才释放
            try {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("释放注册锁异常", e);
            }
        }
    }

    @Override
    public boolean resetPassword(ForgotPasswordResetDTO dto) {
        String username = dto.getUsername();
        String rawPassword = dto.getPassword();
        //todo redis key格式放一块
        String lockKey = "lock:resetPassword:" + username;
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = false;

        try {
            try {
                locked = lock.tryLock(1, 5, TimeUnit.SECONDS);
            } catch (RedisConnectionException | RedisTimeoutException | IllegalStateException e) {
                //todo 后续要高可用可以加乐观锁（版本号）控制并发更新
                log.error("Redis 不可用，重置密码中止", e);
                throw new RuntimeException("系统繁忙，请稍后再试（REDIS 不可用）");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("线程中断，密码重置失败", e);
            }

            if (!locked) {
                throw new RuntimeException("请求过多，请稍后再试");
            }

            // 密码加密
            String encodedPassword = passwordEncoder.encode(rawPassword);

            // 更新数据库
            int rows = userRepository.updatePasswordByUsername(username, encodedPassword);
            if (rows == 0) {
                throw new RuntimeException("用户不存在或已删除");
            }

            return true;

        } finally {
            try {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("释放重置密码锁异常", e);
            }
        }
    }


    @Override
    public boolean resetPasswordAfterLogin(String username, ResetPasswordDTO dto) {
        return false;
    }

    //todo 完成注销动作
    @Override
    public boolean delete(UserDeleteDTO userDeleteDTO) {
        // 删除用户角色关联
        // 逻辑删除用户
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            User user = userRepository.findByUsername(userDeleteDTO.getUsername());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 删除用户角色关联
            userRoleRepository.deleteByUserId(user.getUserId());

            // 逻辑删除用户
            boolean deleted = userRepository.deleteByUsername(userDeleteDTO.getUsername());

            // TODO: 异步清理缓存要做，认证的token都删除
            //  布隆过滤器不一定需要，影响不大，还可以重加载

            return deleted;
        }));
    }

}