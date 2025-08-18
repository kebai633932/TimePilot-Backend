package org.cxk.infrastructure.adapter.dao.converter;

import org.cxk.infrastructure.adapter.dao.po.User;
import org.cxk.domain.model.entity.UserEntity;

/**
 * @author KJH
 * @description
 * @create 2025/8/11 14:30
 */
public class UserConverter {

    public static User converter(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        User po = new User();
        po.setId(entity.getId());
        po.setUserId(entity.getUserId());
        po.setUsername(entity.getUsername());
        po.setPassword(entity.getPassword());
        po.setEmail(entity.getEmail());
        po.setPhone(entity.getPhone());
        po.setIsDeleted(entity.getIsDeleted());
        po.setLastLoginTime(entity.getLastLoginTime());
        po.setDelVersion(entity.getDelVersion());
        // createTime/updateTime 通常由 MyBatis-Plus 自动填充
        return po;
    }

    public static UserEntity converter(User po) {
        if (po == null) {
            return null;
        }
        UserEntity entity = new UserEntity();
        entity.setId(po.getId());
        entity.setUserId(po.getUserId());
        entity.setUsername(po.getUsername());
        entity.setPassword(po.getPassword());
        entity.setEmail(po.getEmail());
        entity.setPhone(po.getPhone());
        entity.setIsDeleted(po.getIsDeleted());
        entity.setLastLoginTime(po.getLastLoginTime());
        entity.setDelVersion(po.getDelVersion());
        // roles 在数据库表没有，需要额外查询后赋值
        return entity;
    }
}
