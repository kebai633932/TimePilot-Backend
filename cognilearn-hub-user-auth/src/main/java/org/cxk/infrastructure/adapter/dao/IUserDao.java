package org.cxk.infrastructure.adapter.dao;

import org.apache.ibatis.annotations.Mapper;
import org.cxk.infrastructure.adapter.dao.po.User;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/6/8 15:59
 */
@Mapper
public interface IUserDao {
    boolean insert(User user);

    User findByUsername(String username);

    List<User> findAll();

}
