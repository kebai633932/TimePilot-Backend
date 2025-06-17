package org.cxk.service.impl;
import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import lombok.RequiredArgsConstructor;
import org.cxk.infrastructure.adapter.dao.po.User;
import org.cxk.infrastructure.adapter.repository.UserRepository;
import org.cxk.model.entity.UserEntity;
import org.cxk.service.IUserAuthService;
import org.cxk.trigger.dto.UserLoginDTO;
import org.cxk.trigger.dto.UserRegisterDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author KJH
 * @description 注册登录服务接口
 * @create 2025/4/25 0:47
 */
@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements IUserAuthService {

    private final UserRepository userRepository;

    //todo 声明为 @Bean 注入方式，而不是每次 new
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public boolean register(UserRegisterDTO userRegisterDTO) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(userRegisterDTO.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 构建 User 对象
        UserEntity user = UserEntity.builder()
                .id(TinyId.nextId("auth_register"))
                .username(userRegisterDTO.getUsername())
                .realName(userRegisterDTO.getUsername()) // 可根据需要调整
                .password(passwordEncoder.encode(userRegisterDTO.getPassword()))
                .email("")
                .phone("")
                .isDeleted(false)
                .build();

        // 保存用户
        return userRepository.save(user);
    }

    @Override
    public boolean login(UserLoginDTO userLoginDTO) {
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

        //3. 登录成功，可以生成 Token（如果有 JWT 模块）
        //todo jwt 三种方法
        return true;
    }

}