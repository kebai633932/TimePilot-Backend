package org.cxk.domain.repository;

import org.cxk.infrastructure.adapter.dao.po.Role;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/7/29 10:33
 */
public interface IRoleRepository {
    List<Role> findRolesByUserId(Long userId);
}
