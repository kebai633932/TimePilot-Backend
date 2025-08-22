package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.cxk.infrastructure.adapter.dao.po.NoteVector;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/8/14 20:40
 */
@Mapper
public interface INoteVectorDao extends BaseMapper<NoteVector> {


    void saveNoteEmbeddings(@Param("noteVectorList") List<NoteVector> noteVectorList);

    void deleteByNoteIds(@Param("noteIds") List<Long> missingNoteIds);

    @Update("UPDATE note_vector SET is_deleted = true, delete_time = NOW() WHERE note_id = #{noteId}")
    void deleteByNoteId(Long noteId);
}
