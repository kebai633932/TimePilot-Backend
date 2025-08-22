package org.cxk.domain.repository;

import org.cxk.domain.model.entity.NoteEntity;

import java.util.List;
import java.util.Optional;

/**
 * @author KJH
 * @description
 * @create 2025/8/16 19:31
 */
public interface INoteRepository {

    int countByParentId(Long folderId);

    void save(NoteEntity noteEntity);

    Optional<NoteEntity> findByNoteIdAndUserId(Long noteId, Long userId);

    void update(NoteEntity noteEntity);
    void move(NoteEntity noteEntity);

    List<NoteEntity> findByUserId(Long userId);

    List<NoteEntity> findByNoteIds(List<Long> batchIds);
}
