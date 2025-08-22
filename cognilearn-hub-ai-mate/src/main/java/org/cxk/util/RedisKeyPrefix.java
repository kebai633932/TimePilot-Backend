package org.cxk.util;

/**
 * @author KJH
 * @description
 * @create 2025/8/14 17:29
 */
public enum RedisKeyPrefix {
    NOTE_VECTOR_TODO("note:vector:todo");


    private final String pattern;

    RedisKeyPrefix(String pattern) {
        this.pattern = pattern;
    }

    public String format(Object... args) {
        return String.format(pattern, args);
    }
}
