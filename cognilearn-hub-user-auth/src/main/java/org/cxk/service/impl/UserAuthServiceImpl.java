package org.cxk.service.impl;
import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.infrastructure.adapter.dao.po.User;

import org.cxk.model.entity.UserEntity;
import org.cxk.service.IUserAuthService;
import org.cxk.service.repository.IUserRepository;
import org.cxk.trigger.dto.UserDeleteDTO;
import org.cxk.trigger.dto.UserRegisterDTO;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

    //todo 用authenticationManager来进行账号密码认证
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;


    private final RedissonClient redissonClient;


    //todo
    @Override
    public boolean register(UserRegisterDTO dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();

        // 3. 获取redis分布式锁（锁粒度按用户名）
        String lockKey = "lock:register:" + username;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 4. 尝试加锁（最多等待3秒，加锁成功后10秒内自动释放）
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new RuntimeException("注册请求过多，请稍后再试");
            }

            // 5. 构建用户实体
            UserEntity user = UserEntity.builder()
                    .id(TinyId.nextId("auth_register"))
                    .username(username)
                    .phone(dto.getPhone())
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(password))
                    .isDeleted(false)
                    .delVersion(0L)
                    .build();

            // 6. 插入数据库，唯一索引兜底
            return userRepository.save(user);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("用户名已存在");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("线程中断，注册失败");
        } finally {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("释放注册锁异常", e); // ✅ 正确使用 @Slf4j 的日志
            }
        }
    }



    @Override
    //todo 使用缓存降低数据库压力
//    @CacheEvict(value = "user", key = "#username")
    public boolean delete(UserDeleteDTO userDeleteDTO) {
        // 1. 查询用户
        User user = userRepository.findByUsername(userDeleteDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 逻辑删除
        return userRepository.deleteByUsername(userDeleteDTO.getUsername());

        //todo redis删除相关数据
        //todo 从布隆过滤器移除
        //todo 需要写入事务表+事务补偿，确保清除干净

    }

}