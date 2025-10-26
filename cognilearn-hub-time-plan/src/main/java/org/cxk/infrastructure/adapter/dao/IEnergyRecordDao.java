package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.cxk.infrastructure.adapter.dao.po.EnergyRecord;

import java.util.Date;
import java.util.List;

/**
 * @author KJH
 * @description 精力分配记录数据访问接口
 * @create 2025/10/26 09:17
 */
@Mapper
public interface IEnergyRecordDao extends BaseMapper<EnergyRecord> {

    /**
     * 根据记录ID查询
     */
    @Select("SELECT * FROM energy_records WHERE id = #{recordId} AND is_deleted = false")
    EnergyRecord findByRecordId(Long recordId);

    /**
     * 根据用户ID和记录日期查询
     */
    @Select("SELECT * FROM energy_records WHERE user_id = #{userId} AND record_date = #{recordDate} AND is_deleted = false")
    EnergyRecord findByUserIdAndDate(Long userId, Date recordDate);

    /**
     * 查询用户的精力记录
     */
    @Select("SELECT * FROM energy_records WHERE user_id = #{userId} AND is_deleted = false ORDER BY record_date DESC")
    List<EnergyRecord> findByUserId(Long userId);

    /**
     * 查询日期范围内的精力记录
     */
    @Select("SELECT * FROM energy_records WHERE user_id = #{userId} AND is_deleted = false " +
            "AND record_date BETWEEN #{startDate} AND #{endDate} ORDER BY record_date ASC")
    List<EnergyRecord> findByDateRange(Long userId, Date startDate, Date endDate);

    /**
     * 逻辑删除精力记录
     */
    @Update("UPDATE energy_records SET is_deleted = true, delete_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{recordId} AND is_deleted = false")
    void deleteByRecordId(Long recordId);

    /**
     * 更新精力水平
     */
    @Update("UPDATE energy_records SET total_energy_level = #{energyLevel}, notes = #{notes}, " +
            "time_slot_energy = #{timeSlotEnergy}, update_time = CURRENT_TIMESTAMP " +
            "WHERE id = #{recordId} AND user_id = #{userId} AND is_deleted = false")
    void updateEnergyRecord(Long recordId, Long userId, Integer energyLevel, String notes, String timeSlotEnergy);
}