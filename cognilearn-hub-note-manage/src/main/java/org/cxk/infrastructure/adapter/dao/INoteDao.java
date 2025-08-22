package org.cxk.infrastructure.adapter.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.cxk.infrastructure.adapter.dao.po.Note;
import org.springframework.data.repository.query.Param;

/**
 * @author KJH
 * @description
 * @create 2025/8/16 19:29
 */
@Mapper
public interface INoteDao  extends BaseMapper<Note> {
    @Update("UPDATE note " +
            "SET title = #{title}, " +
            "    content_md = #{contentMd}, " +
            "    content_plain = #{contentPlain}, " +
            "    folder_id = #{folderId}, " +
            "    status = #{status}, " +
            "    is_public = #{isPublic}, " +
            "    is_deleted = #{isDeleted}, " +
            "    delete_time = #{deleteTime}, " +
            "    update_time = NOW() " +
            "    version = version+1 " +
            "WHERE note_id = #{noteId} AND version = #{version} AND is_deleted = false")
    int updateByNoteId(Note note);
    @Select("SELECT * FROM note WHERE note_id = #{noteId} AND user_id = #{userId} AND is_deleted = false LIMIT 1")
    Note findByNoteIdAndUserId(@Param("noteId") Long noteId, @Param("userId") Long userId);
    @Update("UPDATE note " +
            "SET title = #{title}, " +
            "    content_md = #{contentMd}, " +
            "    content_plain = #{contentPlain}, " +
            "    folder_id = #{folderId}, " +
            "    status = #{status}, " +
            "    is_public = #{isPublic}, " +
            "    is_deleted = #{isDeleted}, " +
            "    delete_time = #{deleteTime}, " +
            "    update_time = NOW() " +
            "WHERE note_id = #{noteId} ")
    int moveByNoteId(Note note);
}
