package org.cxk.domain;

import org.cxk.trigger.dto.TokenPairResponseDTO;

/**
 * @author KJH
 * @description
 * @create 2025/8/4 16:42
 */
public interface ITokenService {
    TokenPairResponseDTO refreshToken(String refreshToken, String currentAccessToken);


    void logout(String accessToken, String refreshToken);
}
