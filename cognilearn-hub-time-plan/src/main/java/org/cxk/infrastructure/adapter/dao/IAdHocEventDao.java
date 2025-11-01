package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.cxk.infrastructure.adapter.dao.po.AdHocEvent;

import java.util.List;

/**
 * @author KJH
 * @description 突发性事件数据访问接口
 * @create 2025/10/26 09:17
 */
@Mapper
public interface IAdHocEventDao extends BaseMapper<AdHocEvent> {

    /**
     * 查询用户的突发事件列表
     */
    @Select("SELECT * FROM ad_hoc_events WHERE user_id = #{userId} AND is_deleted = false ORDER BY create_time DESC")
    List<AdHocEvent> selectByUserId(Long userId);

    /**
     * 根据用户 ID 和事件 ID 逻辑删除
     */
    @Update("UPDATE ad_hoc_events SET is_deleted = true, delete_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{eventId} AND user_id = #{userId} AND is_deleted = false")
    void deleteByUserIdAndId(Long userId, Long eventId);
}
