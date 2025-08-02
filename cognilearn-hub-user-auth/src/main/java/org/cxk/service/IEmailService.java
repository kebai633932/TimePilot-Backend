package org.cxk.service;

import com.google.protobuf.ServiceException;

/**
 * @author KJH
 * @description
 * @create 2025/7/30 14:32
 */
public interface IEmailService {
    void sendEmail(String toEmail, String subject, String content);
    boolean verifyEmailCode(String email, String code) throws ServiceException;

    void sendVerificationEmail(String email) throws ServiceException;
}
