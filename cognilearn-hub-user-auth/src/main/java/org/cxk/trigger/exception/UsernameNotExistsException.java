package org.cxk.trigger.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author KJH
 * @description
 * @create 2025/7/30 13:46
 */
public class UsernameNotExistsException extends AuthenticationException {
    public UsernameNotExistsException(String msg) {
        super(msg);
    }
}
