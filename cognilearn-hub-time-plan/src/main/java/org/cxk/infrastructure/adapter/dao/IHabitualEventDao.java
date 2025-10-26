package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.cxk.infrastructure.adapter.dao.po.HabitualEvent;

import java.util.Date;
import java.util.List;

/**
 * @author KJH
 * @description 习惯性事件数据访问接口
 * @create 2025/10/26 09:17
 */
@Mapper
public interface IHabitualEventDao extends BaseMapper<HabitualEvent> {

    /**
     * 根据事件ID查询
     */
    @Select("SELECT * FROM habitual_events WHERE id = #{eventId} AND is_deleted = false")
    HabitualEvent findByEventId(Long eventId);

    /**
     * 根据事件ID和用户ID查询
     */
    @Select("SELECT * FROM habitual_events WHERE id = #{eventId} AND user_id = #{userId} AND is_deleted = false")
    HabitualEvent findByEventIdAndUserId(Long eventId, Long userId);

    /**
     * 查询用户的所有习惯性事件
     */
    @Select("SELECT * FROM habitual_events WHERE user_id = #{userId} AND is_deleted = false ORDER BY create_time DESC")
    List<HabitualEvent> findByUserId(Long userId);

    /**
     * 根据状态查询用户的习惯性事件
     */
    @Select("SELECT * FROM habitual_events WHERE user_id = #{userId} AND status = #{status} AND is_deleted = false ORDER BY create_time DESC")
    List<HabitualEvent> findByUserIdAndStatus(Long userId, Integer status);

    /**
     * 根据四象限查询用户的习惯性事件
     */
    @Select("SELECT * FROM habitual_events WHERE user_id = #{userId} AND quadrant = #{quadrant} AND is_deleted = false ORDER BY create_time DESC")
    List<HabitualEvent> findByUserIdAndQuadrant(Long userId, Integer quadrant);

    /**
     * 查询指定日期范围内的有效事件
     */
    @Select("SELECT * FROM habitual_events WHERE user_id = #{userId} AND is_deleted = false " +
            "AND status = 1 AND start_date <= #{date} AND (end_date IS NULL OR end_date >= #{date}) " +
            "ORDER BY priority DESC, create_time DESC")
    List<HabitualEvent> findValidEventsByDate(Long userId, Date date);

    /**
     * 逻辑删除习惯性事件
     */
    @Update("UPDATE habitual_events SET is_deleted = true, delete_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{eventId} AND is_deleted = false")
    void deleteByEventId(Long eventId);

    /**
     * 更新完成率
     */
    @Update("UPDATE habitual_events SET completion_rate = #{completionRate}, update_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{eventId} AND user_id = #{userId} AND is_deleted = false")
    void updateCompletionRate(Long eventId, Long userId, Double completionRate);

    /**
     * 更新事件状态
     */
    @Update("UPDATE habitual_events SET status = #{status}, update_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{eventId} AND user_id = #{userId} AND is_deleted = false")
    void updateStatus(Long eventId, Long userId, Integer status);
}