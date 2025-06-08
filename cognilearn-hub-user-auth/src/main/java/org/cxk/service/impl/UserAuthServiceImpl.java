package org.cxk.service.impl;
import com.xiaoju.uemc.tinyid.client.utils.TinyId;
import lombok.RequiredArgsConstructor;
import org.cxk.infrastructure.adapter.repository.UserRepository;
import org.cxk.model.entity.UserEntity;
import org.cxk.service.IUserAuthService;
import org.cxk.trigger.dto.UserRegisterDTO;
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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public boolean register(UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(registerDTO.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 构建 User 对象
        UserEntity user = UserEntity.builder()
                .id(TinyId.nextId("auth_register"))
                .username(registerDTO.getUsername())
                .realName(registerDTO.getUsername()) // 可根据需要调整
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .email("")
                .phone("")
                .isDeleted(false)
                .build();

        // 保存用户
        boolean success=userRepository.save(user);
        return success;
    }

}