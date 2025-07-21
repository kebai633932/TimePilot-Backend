package org.cxk.service.impl;
import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import lombok.RequiredArgsConstructor;
import org.cxk.infrastructure.adapter.dao.po.User;
import org.cxk.infrastructure.adapter.repository.UserRepository;
import org.cxk.model.entity.UserEntity;
import org.cxk.service.IUserAuthService;
import org.cxk.service.repository.IUserRepository;
import org.cxk.trigger.dto.UserDeleteDTO;
import org.cxk.trigger.dto.UserLoginDTO;
import org.cxk.trigger.dto.UserRegisterDTO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author KJH
 * @description 注册登录服务接口
 * @create 2025/4/25 0:47
 */
@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements IUserAuthService {

    private final IUserRepository userRepository;

    //todo 用authenticationManager来进行账号密码认证
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\u4e00-\u9fa5]{3,20}$");

    private final RedissonClient redissonClient;

    public void validateUsername(String username) {
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("用户名不合法：只能包含中英文、数字、下划线，长度3-20");
        }
    }
    @Override
    public boolean register(UserRegisterDTO dto) {
        String username = dto.getUsername();

        // 1. 校验合法性（正则校验）
        validateUsername(username);
        //todo 布隆过滤器预检，防redis穿透
//        if (usernameBloomFilter.mightContain(username)) {
//            throw new BusinessException("用户名可能已存在");
//        }

        //todo 分片锁控制
//        int shard = Math.abs(username.hashCode()) % LOCK_SHARD_NUM;
//        RLock lock = redisson.getLock("user:reg:" + shard);
        // 2. 获取redis分布式锁（锁粒度按用户名）
        String lockKey = "lock:register:" + username;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 3. 尝试加锁（最多等待3秒，加锁成功后10秒内自动释放）
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new RuntimeException("注册请求过多，请稍后再试");
            }

            // 4. 构建用户实体
            UserEntity user = UserEntity.builder()
                    .id(TinyId.nextId("auth_register"))
                    .username(username)
                    .realName(username)
                    .phone("")
                    .email("")
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .isDeleted(false)
                    .delVersion(0L)
                    .build();

            // 5. 插入数据库，唯一索引兜底
            return userRepository.save(user);
            //todo 更新布隆过滤器
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("用户名已存在");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("线程中断，注册失败");
        } finally {
            // 6. 释放锁（确保是当前线程持有的）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    @Override
    public boolean login(UserLoginDTO userLoginDTO) {

        //todo 用authenticationManager来进行账号密码认证

        // 1. 查询用户
        User user = userRepository.findByUsername(userLoginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 校验密码
        boolean match = passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword());
        if (!match) {
            throw new RuntimeException("密码错误");
        }
        System.out.println("登录成功");
        //3. 登录成功，可以生成 Token（如果有 JWT 模块）
        //todo jwt 三种方法，jwt生成jwtutils返回json
        // 同时写入redis缓存
        return true;
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

        //todo 以后 3. 异步清理关联数据

        //todo 需要写入事务表+事务补偿

        //todo 从布隆过滤器移除

    }

}