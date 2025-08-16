package org.cxk.trigger.http;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.service.INoteService;
import org.cxk.trigger.dto.*;
import org.cxk.util.AuthenticationUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import types.enums.ResponseCode;
import types.response.Response;

import java.util.List;

/**
 * @author KJH
 * @description 笔记管理接口
 * @create 2025/8/16
 */
@Slf4j
@RestController
@RequestMapping("/api/note")
@AllArgsConstructor
public class NoteController {

    private final INoteService noteService;

    /**
     * 创建笔记
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Long> createNote(@RequestBody NoteCreateDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();

            Long noteId = noteService.createNote(userId, dto);
            return Response.success(noteId, "笔记创建成功");
        } catch (Exception e) {
            log.error("创建笔记失败，title={}", dto.getTitle(), e);
            return Response.error(ResponseCode.UN_ERROR, "创建失败");
        }
    }

    /**
     * 修改笔记
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Boolean> updateNote(@RequestBody NoteUpdateDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            noteService.updateNote(userId, dto);
            return Response.success(true, "笔记修改成功");
        } catch (Exception e) {
            log.error("修改笔记失败，id={}", dto.getNoteId(), e);
            return Response.error(ResponseCode.UN_ERROR, "修改失败");
        }
    }

    /**
     * 删除笔记（逻辑删除）
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Boolean> deleteNote(@RequestBody NoteDeleteDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();

            noteService.deleteNote(userId, dto.getNoteId());
            return Response.success(true, "删除成功");
        } catch (Exception e) {
            log.error("删除笔记失败，id={}", dto.getNoteId(), e);
            return Response.error(ResponseCode.UN_ERROR, "删除失败");
        }
    }

    /**
     * 查询笔记列表
     */
    @PostMapping("/list")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<List<NoteDTO>> listNotes(@RequestBody NoteQueryDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            List<NoteDTO> notes = noteService.listNotes(userId, dto);
            return Response.success(notes, "查询成功");
        } catch (Exception e) {
            log.error("查询笔记列表失败", e);
            return Response.error(ResponseCode.UN_ERROR, "查询失败");
        }
    }

    /**
     * 搜索笔记（全文/向量搜索）
     */
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<List<NoteDTO>> searchNotes(@RequestBody NoteSearchDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            List<NoteDTO> notes = noteService.searchNotes(userId, dto);
            return Response.success(notes, "搜索成功");
        } catch (Exception e) {
            log.error("笔记搜索失败", e);
            return Response.error(ResponseCode.UN_ERROR, "搜索失败");
        }
    }

    /**
     * 上传笔记图片到 OSS
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();

            // 返回图片 URL
            String url = noteService.uploadNoteImage(userId, file);
            return Response.success(url, "图片上传成功");
        } catch (Exception e) {
            log.error("笔记图片上传失败", e);
            return Response.error(ResponseCode.UN_ERROR, "上传失败");
        }
    }
}
