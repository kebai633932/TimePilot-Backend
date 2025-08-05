package org.cxk.service;

import org.cxk.trigger.dto.TokenPairDTO;

/**
 * @author KJH
 * @description
 * @create 2025/8/4 16:42
 */
public interface ITokenService {
    TokenPairDTO refreshToken(String refreshToken,String currentAccessToken);


    void logout(String accessToken, String refreshToken);
}
