package org.cxk.infrastructure.adapter.repository;

import lombok.AllArgsConstructor;
import org.cxk.infrastructure.adapter.dao.IUserDao;
import org.cxk.infrastructure.adapter.dao.converter.UserConverter;
import org.cxk.infrastructure.adapter.dao.po.User;
import org.cxk.model.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/6/7 22:23
 */
@Repository
@AllArgsConstructor
public class UserRepository {
    private final IUserDao userDao;

    public boolean save(UserEntity userEntity) {
        User user = UserConverter.toPO(userEntity);
        return userDao.insert(user);
    }

    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    public List<User> findAll() {
        return userDao.findAll();
    }
}
