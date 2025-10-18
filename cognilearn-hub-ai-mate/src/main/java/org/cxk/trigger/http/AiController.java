package org.cxk.trigger.http;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.api.IAiApiService;
import org.cxk.api.dto.FlashCardRequestDTO;
import org.cxk.api.dto.FlashCardResponseDTO;
import org.cxk.api.dto.VectorSearchRequestDTO;
import org.cxk.api.dto.VectorSearchResponseDTO;
import org.cxk.api.response.Response;
import org.cxk.domain.IAiService;
import org.cxk.types.enums.ResponseCode;
import org.cxk.util.AuthenticationUtil;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/*
* ai聊天助手（RAG,AGENT），ai接口调用
* */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@AllArgsConstructor
public class AiController implements IAiApiService {

    private final IAiService aiService;

    /** 向量检索（RAG 检索入口）检索相似度最高的文档 */
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<VectorSearchResponseDTO> search(@Valid @RequestBody VectorSearchRequestDTO dto) {
        try {
            VectorSearchResponseDTO result = aiService.vectorSearch(dto);
            return Response.success(result, "检索成功");
        } catch (Exception e) {
            log.error("向量检索失败",e);
            return Response.error(ResponseCode.UN_ERROR, "生成失败");
        }
    }
    //AI 流式对话接口（带 RAG）
//    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public SseEmitter chatStream(@RequestParam String query) {
//        try {
//            Long userId = AuthenticationUtil.getCurrentUserId();
//            return aiService.chatWithStream(userId, query);
//        } catch (Exception e) {
//            log.error("SSE 对话流式生成失败, query={}", query, e);
//            throw new RuntimeException("对话失败");
//        }
//    }
    //   chat/stop —— 终止流式生成
    //   chat/history —— 历史记录
    //   chat/session —— 会话管理


    // 一键生成今日复习卡片  todo: 未完成
    @PostMapping("/flashcards/today")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<FlashCardResponseDTO> generateTodayCards() {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            FlashCardResponseDTO cards = aiService.generateFlashCards(userId);
            return Response.success(cards, "生成成功");
        } catch (Exception e) {
            log.error("生成复习卡片失败",e);
            return Response.error(ResponseCode.UN_ERROR, "生成失败");
        }
    }
    /** 生成复习卡片 */
    @PostMapping("/flashcards")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<FlashCardResponseDTO> generateCards(@Valid @RequestBody FlashCardRequestDTO dto) {
        try {
            FlashCardResponseDTO cards = aiService.generateFlashCards(dto);
            return Response.success(cards, "生成成功");
        } catch (Exception e) {
            log.error("生成复习卡片失败",e);
            return Response.error(ResponseCode.UN_ERROR, "生成失败");
        }
    }

/** 根据笔记生成发帖草稿 */
//    todo 后续做
//    @PostMapping("/draft-post")
//    @PreAuthorize("hasAnyRole('USER','ADMIN')")
//    public Response<> draftPost(@RequestBody PostDraftDTO dto) {
//        try {
//            var draft = aiService.generatePostDraft(req);
//            return Response.success(draft, "草稿已生成");
//        } catch (Exception e) {
//            log.error("生成发帖草稿失败, req={}", req, e);
//            return Response.error(ResponseCode.UN_ERROR, "生成失败");
//        }
//    }
}
