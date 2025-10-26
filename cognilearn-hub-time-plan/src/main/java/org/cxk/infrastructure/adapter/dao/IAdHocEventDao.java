package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.cxk.infrastructure.adapter.dao.po.AdHocEvent;

import java.util.Date;
import java.util.List;

/**
 * @author KJH
 * @description 突发性事件数据访问接口
 * @create 2025/10/26 09:17
 */
@Mapper
public interface IAdHocEventDao extends BaseMapper<AdHocEvent> {

    /**
     * 根据事件ID查询
     */
    @Select("SELECT * FROM ad_hoc_events WHERE id = #{eventId} AND is_deleted = false")
    AdHocEvent findByEventId(Long eventId);

    /**
     * 根据事件ID和用户ID查询
     */
    @Select("SELECT * FROM ad_hoc_events WHERE id = #{eventId} AND user_id = #{userId} AND is_deleted = false")
    AdHocEvent findByEventIdAndUserId(Long eventId, Long userId);

    /**
     * 查询用户的所有突发性事件
     */
    @Select("SELECT * FROM ad_hoc_events WHERE user_id = #{userId} AND is_deleted = false ORDER BY create_time DESC")
    List<AdHocEvent> findByUserId(Long userId);

    /**
     * 根据状态查询用户的突发性事件
     */
    @Select("SELECT * FROM ad_hoc_events WHERE user_id = #{userId} AND status = #{status} AND is_deleted = false ORDER BY deadline ASC")
    List<AdHocEvent> findByUserIdAndStatus(Long userId, Integer status);

    /**
     * 查询今日待办的突发性事件
     */
    @Select("SELECT * FROM ad_hoc_events WHERE user_id = #{userId} AND is_deleted = false " +
            "AND status IN (1, 2) AND DATE(planned_start_time) = CURRENT_DATE " +
            "ORDER BY priority DESC, planned_start_time ASC")
    List<AdHocEvent> findTodayEvents(Long userId);

    /**
     * 查询逾期未完成的突发性事件
     */
    @Select("SELECT * FROM ad_hoc_events WHERE user_id = #{userId} AND is_deleted = false " +
            "AND status IN (1, 2) AND deadline < CURRENT_TIMESTAMP " +
            "ORDER BY deadline ASC")
    List<AdHocEvent> findOverdueEvents(Long userId);

    /**
     * 根据截止时间范围查询
     */
    @Select("SELECT * FROM ad_hoc_events WHERE user_id = #{userId} AND is_deleted = false " +
            "AND deadline BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY deadline ASC")
    List<AdHocEvent> findByDeadlineRange(Long userId, Date startTime, Date endTime);

    /**
     * 逻辑删除突发性事件
     */
    @Update("UPDATE ad_hoc_events SET is_deleted = true, delete_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{eventId} AND is_deleted = false")
    void deleteByEventId(Long eventId);

    /**
     * 更新事件状态
     */
    @Update("UPDATE ad_hoc_events SET status = #{status}, update_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{eventId} AND user_id = #{userId} AND is_deleted = false")
    void updateStatus(Long eventId, Long userId, Integer status);

    /**
     * 记录实际时间
     */
    @Update("UPDATE ad_hoc_events SET actual_start_time = #{actualStartTime}, " +
            "actual_end_time = #{actualEndTime}, actual_hours = #{actualHours}, " +
            "update_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{eventId} AND user_id = #{userId} AND is_deleted = false")
    void recordActualTime(Long eventId, Long userId, Date actualStartTime, Date actualEndTime, Double actualHours);
}