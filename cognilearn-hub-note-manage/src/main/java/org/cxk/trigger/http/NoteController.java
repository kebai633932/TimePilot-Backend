//package org.cxk.trigger.http;
//
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.cxk.service.INoteService;
//import org.cxk.trigger.dto.*;
//import org.cxk.util.AuthenticationUtil;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import types.enums.ResponseCode;
//import types.response.Response;
//
//import java.util.List;
//
///**
// * @author KJH
// * @description 笔记管理
// * @create 2025/8/14
// */
//@Slf4j
//@RestController
//@RequestMapping("/api/note")
//@AllArgsConstructor
//public class NoteController {
//
//    private final INoteService noteService;
//
//    /**
//     * 创建笔记
//     *
//     * @param dto NoteCreateDTO 包含标题、内容、文件夹ID、是否公开等信息
//     * @return Response<Long> 返回新建笔记ID
//     */
//    @PostMapping("/create")
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public Response<Long> createNote(@RequestBody NoteCreateDTO dto) {
//        try {
//            Long userId = AuthenticationUtil.getCurrentUserId();
//            // todo 需要同时发送 SSE + 旁路缓存
//            Long noteId = noteService.createNote(userId, dto);
//            return Response.success(noteId, "笔记创建成功");
//        } catch (Exception e) {
//            log.error("创建笔记失败，title={}", dto.getTitle(), e);
//            return Response.error(ResponseCode.UN_ERROR, "创建失败");
//        }
//    }
//
//    /**
//     * 修改笔记内容（标题、内容、是否公开、移动文件夹）
//     *
//     * @param dto NoteUpdateDTO 包含笔记ID、标题、内容、文件夹ID、是否公开
//     * @return Response<Boolean> 修改是否成功
//     */
//    @PostMapping("/update")
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public Response<Boolean> updateNote(@RequestBody NoteUpdateDTO dto) {
//        try {
//            Long userId = AuthenticationUtil.getCurrentUserId();
//            // todo 需要判断笔记是否属于 userId
//            // todo 更新后发送 SSE + 旁路缓存
//            noteService.updateNote(userId, dto);
//            return Response.success(true, "笔记修改成功");
//        } catch (Exception e) {
//            log.error("修改笔记失败，id={}", dto.getNoteId(), e);
//            return Response.error(ResponseCode.UN_ERROR, "修改失败");
//        }
//    }
//
//    /**
//     * 删除笔记（逻辑删除）
//     *
//     * @param dto NoteDeleteDTO 包含笔记ID
//     * @return Response<Boolean> 删除是否成功
//     */
//    @PostMapping("/delete")
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public Response<Boolean> deleteNote(@RequestBody NoteDeleteDTO dto) {
//        try {
//            Long userId = AuthenticationUtil.getCurrentUserId();
//            // todo 更新后发送 SSE + 旁路缓存
//            noteService.deleteNote(userId, dto.getNoteId());
//            return Response.success(true, "笔记删除成功");
//        } catch (Exception e) {
//            log.error("删除笔记失败，id={}", dto.getNoteId(), e);
//            return Response.error(ResponseCode.UN_ERROR, "删除失败");
//        }
//    }
//
//    /**
//     * 查询笔记列表（支持分页、过滤）
//     *
//     * @param dto NoteQueryDTO 包含用户ID、文件夹ID等条件
//     * @return Response<List<NoteDTO>> 笔记列表
//     */
//    @PostMapping("/list")
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public Response<List<NoteDTO>> listNotes(@RequestBody NoteQueryDTO dto) {
//        try {
//            Long userId = AuthenticationUtil.getCurrentUserId();
//            List<NoteDTO> notes = noteService.listNotes(userId, dto);
//            return Response.success(notes, "查询成功");
//        } catch (Exception e) {
//            log.error("查询笔记列表失败，userId={}", AuthenticationUtil.getCurrentUserId(), e);
//            return Response.error(ResponseCode.UN_ERROR, "查询失败");
//        }
//    }
//
//    /**
//     * 搜索笔记（全文搜索 + 向量搜索）
//     *
//     * @param dto NoteSearchDTO 包含关键词、搜索类型等
//     * @return Response<List<NoteDTO>> 搜索结果
//     */
//    @PostMapping("/search")
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public Response<List<NoteDTO>> searchNotes(@RequestBody NoteSearchDTO dto) {
//        try {
//            Long userId = AuthenticationUtil.getCurrentUserId();
//            List<NoteDTO> notes = noteService.searchNotes(userId, dto);
//            return Response.success(notes, "搜索成功");
//        } catch (Exception e) {
//            log.error("笔记搜索失败，userId={}", AuthenticationUtil.getCurrentUserId(), e);
//            return Response.error(ResponseCode.UN_ERROR, "搜索失败");
//        }
//    }
//}
