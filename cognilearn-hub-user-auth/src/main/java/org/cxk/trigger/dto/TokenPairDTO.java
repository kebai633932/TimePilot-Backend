package org.cxk.trigger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author KJH
 * @description
 * @create 2025/8/4 14:11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenPairDTO implements Serializable {
    private String accessToken;
    private String refreshToken;
}