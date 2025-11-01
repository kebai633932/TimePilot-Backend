package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.cxk.infrastructure.adapter.dao.po.EventTag;

import java.util.List;

/**
 * @author KJH
 * @description 事件标签关联数据访问接口
 * @create 2025/10/26 09:17
 */
@Mapper
public interface IEventTagDao extends BaseMapper<EventTag> {

    /**
     * 根据关联ID查询
     */
    @Select("SELECT * FROM event_tags WHERE id = #{id} AND is_deleted = false")
    EventTag findById(Long id);

    /**
     * 根据事件查询标签关联
     */
    @Select("SELECT * FROM event_tags WHERE event_type = #{eventType} AND event_id = #{eventId} AND is_deleted = false")
    List<EventTag> findByEvent(Integer eventType, Long eventId);

    /**
     * 根据标签查询事件关联
     */
    @Select("SELECT * FROM event_tags WHERE tag_id = #{tagId} AND is_deleted = false")
    List<EventTag> findByTagId(Long tagId);

    /**
     * 检查关联是否存在
     */
    @Select("SELECT COUNT(*) FROM event_tags WHERE event_type = #{eventType} AND event_id = #{eventId} " +
            "AND tag_id = #{tagId} AND is_deleted = false")
    int existsByEventAndTag(Integer eventType, Long eventId, Long tagId);

    /**
     * 逻辑删除事件标签关联
     */
    @Update("UPDATE event_tags SET is_deleted = true, delete_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{id} AND is_deleted = false")
    void deleteById(Long id);

    /**
     * 删除事件的所有标签关联
     */
    @Update("UPDATE event_tags SET is_deleted = true, delete_time = CURRENT_TIMESTAMP " +
            "WHERE event_type = #{eventType} AND event_id = #{eventId} AND is_deleted = false")
    void deleteByEvent(Integer eventType, Long eventId);

    /**
     * 批量插入事件标签关联
     */
    @Select({
            "<script>",
            "INSERT INTO event_tags (event_type, event_id, tag_id) VALUES ",
            "<foreach collection='tagIds' item='tagId' separator=','>",
            "(#{eventType}, #{eventId}, #{tagId})",
            "</foreach>",
            "</script>"
    })
    void batchInsert(Integer eventType, Long eventId, List<Long> tagIds);
}