package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;
import org.cxk.infrastructure.adapter.dao.po.User;
import org.cxk.infrastructure.adapter.dao.po.UserRole;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/7/31 18:40
 */
public interface IUserRoleDao  extends BaseMapper<UserRole> {
    // @Insert 不支持循环
    void insertBatchSomeColumn(List<UserRole> userRoles);

    @Update("UPDATE user_role " +
            "SET is_deleted = 1, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId} AND is_deleted = 0")
    int deleteByUserId(@Param("userId") Long userId);
}
