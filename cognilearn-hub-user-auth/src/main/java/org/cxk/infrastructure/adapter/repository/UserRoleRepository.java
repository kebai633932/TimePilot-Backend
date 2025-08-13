package org.cxk.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.cxk.infrastructure.adapter.dao.IUserDao;
import org.cxk.infrastructure.adapter.dao.IUserRoleDao;
import org.cxk.infrastructure.adapter.dao.po.User;
import org.cxk.infrastructure.adapter.dao.po.UserRole;
import org.cxk.service.repository.IUserRepository;
import org.cxk.service.repository.IUserRoleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author KJH
 * @description
 * @create 2025/7/31 18:39
 */
@Repository
@AllArgsConstructor
public class UserRoleRepository implements IUserRoleRepository {

    private final IUserRoleDao userRoleDao;

    @Override
    public void insertUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<UserRole> userRoles = roleIds.stream().map(roleId -> {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            return userRole;
        }).collect(Collectors.toList());
        userRoleDao.insertBatchSomeColumn(userRoles); // 批量插入（MP默认没有批量插入，需要扩展方法）
    }
    //这个暂时没用
    @Override
    public List<Long> findRoleIdsByUserId(Long userId) {
        return userRoleDao.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
//                        .eq(UserRole::getIsDeleted, false) // 逻辑未删除
        ).stream().map(UserRole::getRoleId).collect(Collectors.toList());
    }

    @Override
    public void deleteByUserId(Long id) {
        userRoleDao.deleteByUserId(id);
    }


}

