package org.cxk.domain;

/**
 * @author KJH
 * @description
 * @create 2025/7/30 16:19
 */
public interface ISmsService {
    void sendVerificationCode(String phone);
    void validateVerificationCode(String phone, String code);
}
