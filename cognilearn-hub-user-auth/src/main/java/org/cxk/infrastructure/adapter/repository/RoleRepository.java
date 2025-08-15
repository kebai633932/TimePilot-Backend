package org.cxk.infrastructure.adapter.repository;

import lombok.AllArgsConstructor;
import org.cxk.infrastructure.adapter.dao.IRoleDao;
import org.cxk.infrastructure.adapter.dao.po.Role;
import org.cxk.model.entity.RoleEntity;
import org.cxk.service.repository.IRoleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/7/29 10:35
 */
@Repository
@AllArgsConstructor
public class RoleRepository implements IRoleRepository {

    private final IRoleDao roleDao;

    @Override
    public  List<Role> findRolesByUserId(Long userId) {
        return roleDao.findByUserId(userId);
    }
}