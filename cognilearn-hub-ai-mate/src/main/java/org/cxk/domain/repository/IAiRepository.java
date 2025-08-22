package org.cxk.domain.repository;

import org.cxk.domain.model.entity.NoteVectorEntity;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/8/14 15:26
 */
public interface IAiRepository {
    void saveNoteEmbedding(List<NoteVectorEntity> noteVectorEntityList);

    void deleteByNoteIds(List<Long> missingNoteIds);
}
