package org.cxk.application.impl;

import lombok.extern.slf4j.Slf4j;
import org.cxk.api.dto.NoteCreateDTO;
import org.cxk.api.dto.NoteDTO;
import org.cxk.api.dto.NoteQueryDTO;
import org.cxk.api.dto.NoteUpdateDTO;
import org.cxk.application.INoteAppService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author KJH
 * @description 笔记管理服务接口
 * @create 2025/4/25 0:46
 */
@Slf4j
@Service
public class NoteAppServiceImpl implements INoteAppService {

    @Override
    public Long createNote(Long userId, NoteCreateDTO dto) {
        return null;
    }

    @Override
    public void updateNote(Long userId, NoteUpdateDTO dto) {

    }

    @Override
    public void deleteNote(Long userId, Long noteId) {

    }

    @Override
    public List<NoteDTO> listNotes(Long userId, NoteQueryDTO dto) {
        return null;
    }
}
