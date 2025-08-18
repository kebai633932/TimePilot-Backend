package org.cxk.domain;

import org.cxk.api.dto.NoteCreateDTO;
import org.cxk.api.dto.NoteDTO;
import org.cxk.api.dto.NoteQueryDTO;
import org.cxk.api.dto.NoteUpdateDTO;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/4/25 0:56
 */
public interface INoteDomainService {

    Long createNote(Long userId, NoteCreateDTO dto);

    void updateNote(Long userId, NoteUpdateDTO dto);

    void deleteNote(Long userId, Long noteId);

    List<NoteDTO> listNotes(Long userId, NoteQueryDTO dto);
}
