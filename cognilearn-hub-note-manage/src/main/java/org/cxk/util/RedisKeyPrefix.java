package org.cxk.util;

/**
 * @author KJH
 * @description
 * @create 2025/8/14 17:29
 */
public enum RedisKeyPrefix {
    USER_NOTE_LIST("user:%s:note:list"),
    NOTE_INFO("note:%s:info"),
    FOLDER_INFO("folder:%s:info"),
    USER_FOLDER_LIST("user:%s:folder:list");



    private final String pattern;

    RedisKeyPrefix(String pattern) {
        this.pattern = pattern;
    }

    public String format(Object... args) {
        return String.format(pattern, args);
    }
}
