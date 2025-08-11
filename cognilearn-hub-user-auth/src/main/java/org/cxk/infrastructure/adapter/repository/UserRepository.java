package org.cxk.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.cxk.infrastructure.adapter.dao.IUserDao;
import org.cxk.infrastructure.adapter.dao.converter.UserConverter;
import org.cxk.infrastructure.adapter.dao.po.User;
import org.cxk.model.entity.UserEntity;
import org.cxk.service.repository.IUserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author KJH
 * @description
 * @create 2025/6/7 22:23
 */
@Repository
@AllArgsConstructor
public class UserRepository implements IUserRepository {

    private final IUserDao userDao;

    public boolean save(UserEntity userEntity) {
        User user = UserConverter.converter(userEntity);
        return userDao.insert(user) > 0;
    }

    public List<String> getAllUsernames() {
        return userDao.selectList(
                new LambdaQueryWrapper<User>().select(User::getUsername)
        ).stream().map(User::getUsername).collect(Collectors.toList());
    }

    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        wrapper.eq(User::getIsDeleted, false); // 增加逻辑删除条件
        return userDao.selectOne(wrapper);
    }

    @Override
    public List<User> findAll() {
        return userDao.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getIsDeleted, false)
        );
    }

    public boolean deleteByUsername(String username) {
        if (userDao.deleteByUsername(username) == 1) {
            return true;
        } else if (userDao.deleteByUsername(username) == 0) {
            throw new RuntimeException("用户名重名");
        }
        return false;
    }

    @Override
    public int countByEmail(String email) {
        return userDao.countByEmail(email);
    }

    @Override
    public int countByPhone(String phone) {

        return userDao.countByPhone(phone);
    }

    @Override
    public int updatePasswordByUsername(String username, String encodedPassword) {
        return userDao.updatePasswordByUsername(username,encodedPassword);
    }
}
