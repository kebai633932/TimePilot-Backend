package org.cxk.trigger.http;

import api.response.Response;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.api.dto.*;
import org.cxk.application.IFolderAppService;
import org.cxk.domain.INoteDomainService;
import org.cxk.util.AuthenticationUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import types.enums.ResponseCode;

import java.util.List;
import java.util.Map;

/**
 * @author KJH
 * @description 笔记管理接口
 * @create 2025/8/16
 */
@Slf4j
@RestController
@RequestMapping("/api/note")
@AllArgsConstructor
public class NoteManageController{

    private final INoteDomainService noteService;
    private final IFolderAppService folderService;
    /**
     * 创建笔记
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Long> createNote(@Valid @RequestBody NoteCreateDTO dto) {
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
     * 移动笔记
     */
    @PostMapping("/move")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Boolean> moveNote(@Valid @RequestBody NoteMoveDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            noteService.moveNote(userId, dto);
            return Response.success(true, "笔记修改成功");
        } catch (Exception e) {
            log.error("修改笔记失败，id={}", dto.getNoteId(), e);
            return Response.error(ResponseCode.UN_ERROR, "修改失败");
        }
    }
    /**
     * 修改笔记内容
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Boolean> updateNote(@Valid @RequestBody NoteUpdateDTO dto) {
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
    public Response<Boolean> deleteNote(@Valid @RequestBody NoteDeleteDTO dto) {
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
     * 查询笔记树（文件夹 + 笔记）
     */
    @PostMapping("/listInfo")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<FolderNoteDTO> listFolderNotes() {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            // 1. 获取用户所有文件夹信息 map
            Map<Long, FolderNoteDTO> folderNoteDTOMap = folderService.getFolderMap(userId);
            // 2. 给每个文件夹挂载笔记
            List<NoteInfoDTO> rootNotes = noteService.attachNotesToFolders(userId, folderNoteDTOMap);
            // 3.构建用户的文件夹树（每个文件夹下的子文件夹）
            FolderNoteDTO folderTree = folderService.buildFolderTree(folderNoteDTOMap,rootNotes);
            return Response.success(folderTree, "查询成功");
        } catch (Exception e) {
            log.error("查询笔记树失败", e);
            return Response.error(ResponseCode.UN_ERROR, "查询失败");
        }
    }

    /**
     * todo 搜索笔记（ES做全文搜索） 以后再做
     */
//    @PostMapping("/search")
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public Response<List<NoteDTO>> searchNotes(@RequestBody NoteSearchDTO dto) {
//        try {
//            Long userId = AuthenticationUtil.getCurrentUserId();
//            List<NoteDTO> notes = noteService.searchNotes(userId, dto);
//            return Response.success(notes, "搜索成功");
//        } catch (Exception e) {
//            log.error("笔记搜索失败", e);
//            return Response.error(ResponseCode.UN_ERROR, "搜索失败");
//        }
//    }

    /**
     * 上传笔记图片到 OSS
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<String> uploadImage(@Valid @RequestParam("file") MultipartFile file) {
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
