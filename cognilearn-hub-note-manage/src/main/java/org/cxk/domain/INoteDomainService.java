package org.cxk.domain;

import org.cxk.api.INoteService;
import org.cxk.api.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author KJH
 * @description
 * @create 2025/4/25 0:56
 */
public interface INoteDomainService{

    Long createNote(Long userId, NoteCreateDTO dto);

    void moveNote(Long userId, NoteMoveDTO dto);

    void updateNote(Long userId, NoteUpdateDTO dto);

    void deleteNote(Long userId, Long noteId);


    String uploadNoteImage(Long userId, MultipartFile file);

    List<NoteInfoDTO> attachNotesToFolders(Long userId, Map<Long, FolderNoteDTO> folderNoteDTOMap);
}
