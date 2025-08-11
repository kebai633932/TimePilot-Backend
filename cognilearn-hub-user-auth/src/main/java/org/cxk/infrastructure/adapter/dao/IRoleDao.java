package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.cxk.infrastructure.adapter.dao.po.Role;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/7/29 10:35
 */
@Mapper
public interface IRoleDao extends BaseMapper<Role> {

    @Select("SELECT r.* FROM role r " +
            "JOIN user_role ur ON r.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.is_deleted = 0")
    List<Role> findByUserId(Long userId);
}
