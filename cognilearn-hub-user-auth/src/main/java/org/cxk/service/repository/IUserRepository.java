package org.cxk.service.repository;

import org.cxk.infrastructure.adapter.dao.converter.UserConverter;
import org.cxk.infrastructure.adapter.dao.po.User;
import org.cxk.model.entity.UserEntity;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/6/8 16:36
 */
public interface IUserRepository {

    public boolean save(UserEntity userEntity);

    public User findByUsername(String username);
    public List<String> getAllUsernames();
    public List<User> findAll();
    public boolean deleteByUsername(String username);
}
