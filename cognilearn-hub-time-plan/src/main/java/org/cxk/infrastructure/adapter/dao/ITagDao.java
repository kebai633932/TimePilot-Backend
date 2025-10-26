package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.cxk.infrastructure.adapter.dao.po.Tag;

import java.util.List;

/**
 * @author KJH
 * @description 标签数据访问接口
 * @create 2025/10/26 09:17
 */
@Mapper
public interface ITagDao extends BaseMapper<Tag> {

    /**
     * 根据标签ID查询
     */
    @Select("SELECT * FROM tags WHERE id = #{tagId} AND is_deleted = false")
    Tag findByTagId(Long tagId);

    /**
     * 根据用户ID和标签名称查询
     */
    @Select("SELECT * FROM tags WHERE user_id = #{userId} AND name = #{name} AND is_deleted = false")
    Tag findByUserIdAndName(Long userId, String name);

    /**
     * 查询用户的所有标签
     */
    @Select("SELECT * FROM tags WHERE user_id = #{userId} AND is_deleted = false ORDER BY create_time DESC")
    List<Tag> findByUserId(Long userId);

    /**
     * 根据标签ID和用户ID查询
     */
    @Select("SELECT * FROM tags WHERE id = #{tagId} AND user_id = #{userId} AND is_deleted = false")
    Tag findByTagIdAndUserId(Long tagId, Long userId);

    /**
     * 逻辑删除标签
     */
    @Update("UPDATE tags SET is_deleted = true, delete_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{tagId} AND is_deleted = false")
    void deleteByTagId(Long tagId);

    /**
     * 批量查询标签
     */
    @Select({
            "<script>",
            "SELECT * FROM tags WHERE id IN",
            "<foreach collection='tagIds' item='tagId' open='(' separator=',' close=')'>",
            "#{tagId}",
            "</foreach>",
            "AND is_deleted = false",
            "</script>"
    })
    List<Tag> findByIds(List<Long> tagIds);
}