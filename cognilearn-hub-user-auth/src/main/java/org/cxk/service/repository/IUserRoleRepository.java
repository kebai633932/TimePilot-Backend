package org.cxk.service.repository;

import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/7/31 18:38
 */
public interface IUserRoleRepository {
    // 批量插入用户-角色关系
    void insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
    // 查询用户的所有角色 ID
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long id);
}
