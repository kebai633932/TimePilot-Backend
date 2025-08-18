package org.cxk.domain;

/**
 * @author KJH
 * @description
 * @create 2025/7/30 14:32
 */
public interface IEmailService {
    void sendEmail(String toEmail, String subject, String content);
    boolean verifyEmailCode(String email, String code);

    void sendVerificationEmail(String email);
}
