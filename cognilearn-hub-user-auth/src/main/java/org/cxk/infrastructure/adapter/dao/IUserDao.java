package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.cxk.infrastructure.adapter.dao.po.User;

import java.util.List;

@Mapper
public interface IUserDao extends BaseMapper<User> {

    @Select("SELECT * FROM `user` WHERE username = #{username} AND is_deleted = 0")
    User findByUsername(String username);

    // 如果不额外条件，可以不写此方法，用BaseMapper.selectList(null)
    @Select("SELECT * FROM `user` WHERE is_deleted = 0")
    List<User> findAll();

    @Update("UPDATE `user` " +
            "SET is_deleted = 1, " +
            "del_version = id, -- 直接将主键id赋值给del_version " +
            "update_time = NOW() " +
            "WHERE username = #{username} AND is_deleted = 0")
    int deleteByUsername(String username);
}
