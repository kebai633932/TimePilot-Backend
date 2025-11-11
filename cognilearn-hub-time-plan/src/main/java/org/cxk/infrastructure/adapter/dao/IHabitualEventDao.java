package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.cxk.infrastructure.adapter.dao.po.HabitualEvent;

import java.util.List;

/**
 * @author KJH
 * @description 习惯性事件数据访问接口
 * @create 2025/10/26 09:17
 */
@Mapper
public interface IHabitualEventDao extends BaseMapper<HabitualEvent> {

    /**
     * 查询用户的所有习惯性事件（未删除）
     */
    @Select("""
            SELECT *
            FROM habitual_events
            WHERE user_id = #{userId}
              AND is_deleted = false
            ORDER BY create_time DESC
            """)
    List<HabitualEvent> selectByUserId(Long userId);
}
