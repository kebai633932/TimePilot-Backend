package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.cxk.infrastructure.adapter.dao.po.Folder;
import org.cxk.infrastructure.adapter.dao.po.Role;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/8/14 20:40
 */
@Mapper
public interface IFolderDao extends BaseMapper<Folder> {

    @Select("SELECT * FROM folder WHERE folder_id = #{folderId} AND is_deleted = false")
    Folder findByFolderId(Long folderId);

    /**
     * 逻辑删除文件夹
     * 1. 将 is_deleted 标记为 true
     * 2. 记录删除时间 deleted_at
     */
    @Update(
            "UPDATE folder " +
                    "SET is_deleted = true, delete_time = NOW() " +
                    "WHERE folder_id = #{folderId} AND is_deleted = false"
    )
    void deleteByFolderId(Long folderId);
}
