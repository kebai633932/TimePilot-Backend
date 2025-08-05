package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.cxk.infrastructure.adapter.dao.po.User;
import org.springframework.data.repository.query.Param;

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


    // 查询拥有该邮箱的用户数量
    @Select("SELECT COUNT(*) FROM `user` WHERE email = #{email} AND is_deleted = 0")
    int countByEmail(@Param("email") String email);

    // 查询拥有该手机号的用户数量
    @Select("SELECT COUNT(*) FROM `user` WHERE phone = #{phone} AND is_deleted = 0")
    int countByPhone(@Param("phone") String phone);
    @Update("UPDATE `user` " +
            "SET password = #{encodedPassword}  " +
            "WHERE username = #{username} AND is_deleted = 0")
    int updatePasswordByUsername(String username, String encodedPassword);
}
