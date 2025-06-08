package org.cxk.infrastructure.adapter.dao.converter;

import org.cxk.infrastructure.adapter.dao.po.User;
import org.cxk.model.entity.UserEntity;

import java.util.Date;

/**
 * @author KJH
 * @description
 * @create 2025/6/8 16:37
 */
public class UserConverter {

    public static User toPO(UserEntity entity) {
        if (entity == null) return null;

        User user = new User();
        user.setId(entity.getId());
        user.setUsername(entity.getUsername());
        user.setRealName(entity.getRealName());
        user.setPassword(entity.getPassword());
        user.setEmail(entity.getEmail());
        user.setPhone(entity.getPhone());
        user.setIsDeleted(entity.getIsDeleted());
        user.setLastLoginTime(entity.getLastLoginTime());
        user.setCreateTime(new Date()); // 或者 entity.getCreateTime()
        user.setUpdateTime(new Date()); // 或者 entity.getUpdateTime()
        // TODO: 角色转换(需要时)
        return user;
    }

    public static UserEntity toEntity(User user) {
        if (user == null) return null;

        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setRealName(user.getRealName());
        entity.setPassword(user.getPassword());
        entity.setEmail(user.getEmail());
        entity.setPhone(user.getPhone());
        entity.setIsDeleted(user.getIsDeleted());
        entity.setLastLoginTime(user.getLastLoginTime());
        // TODO: 角色转换(需要时)
        return entity;
    }
}

