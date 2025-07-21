package org.cxk.service;

import org.cxk.trigger.dto.UserDeleteDTO;
import org.cxk.trigger.dto.UserLoginDTO;
import org.cxk.trigger.dto.UserRegisterDTO;

/**
 * @author KJH
 * @description
 * @create 2025/4/25 1:00
 */
public interface IUserAuthService {
    boolean register(UserRegisterDTO userRegisterDTO);
    boolean login(UserLoginDTO userLoginDTO);
    boolean delete(UserDeleteDTO userDeleteDTO);
}
