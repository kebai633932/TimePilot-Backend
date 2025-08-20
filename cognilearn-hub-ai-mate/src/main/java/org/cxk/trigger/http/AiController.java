package org.cxk.trigger.http;

import api.response.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.api.IAiApiService;
import org.cxk.api.dto.FlashCardDTO;
import org.cxk.api.dto.PostDraftDTO;
import org.cxk.api.dto.SearchDTO;
import org.cxk.domain.IAiService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import types.enums.ResponseCode;
/*
* ai聊天助手（RAG,AGENT），ai接口调用
* */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@AllArgsConstructor
public class AiController implements IAiApiService {

    private final IAiService aiService;

    /** 向量检索（RAG 检索入口） */
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<> search(@RequestBody SearchDTO dto) {
        try {
             result = aiService.vectorSearch(req);
            return Response.success(result, "检索成功");
        } catch (Exception e) {
            log.error("AI 向量检索失败, req={}", req, e);
            return Response.error(ResponseCode.UN_ERROR, "检索失败");
        }
    }

    /** 生成复习卡片 */
    @PostMapping("/flashcards")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<List<AiDTO.FlashCard>> generateCards(@RequestBody FlashCardDTO dto) {
        try {
            var cards = aiService.generateFlashCards(req);
            return Response.success(cards, "生成成功");
        } catch (Exception e) {
            log.error("生成复习卡片失败, req={}", req, e);
            return Response.error(ResponseCode.UN_ERROR, "生成失败");
        }
    }

    /** 根据笔记生成发帖草稿 */
    @PostMapping("/draft-post")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<AiDTO.PostDraft> draftPost(@RequestBody PostDraftDTO dto) {
        try {
            var draft = aiService.generatePostDraft(req);
            return Response.success(draft, "草稿已生成");
        } catch (Exception e) {
            log.error("生成发帖草稿失败, req={}", req, e);
            return Response.error(ResponseCode.UN_ERROR, "生成失败");
        }
    }
}
