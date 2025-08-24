package org.cxk.domain.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.cxk.api.INoteService;
import org.cxk.api.dto.*;
import org.cxk.domain.IAiService;
import org.cxk.domain.model.entity.NoteVectorEntity;
import org.cxk.domain.repository.IAiRepository;
import org.cxk.types.exception.BizException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
/**
 * @author KJH
 * @description AI 服务实现类
 *              - 包含向量检索（RAG）
 *              - 复习卡片生成
 *              - 支持按用户生成今日复习卡片
 * @create 2025/8/21
 */
@Slf4j
@Service
public class AiServiceImpl implements IAiService {
    @DubboReference(version = "1.0")
    private INoteService noteService;

    @Resource
    private IAiRepository aiRepository;
    //    todo 第三方接口应该放哪里？service,repository
    @Resource
    private OpenAiEmbeddingModel embeddingModel;       // PgVectorStore 向量存储
    @Resource
    private OpenAiChatModel openAiChatModel;       // Chat 模型，用于生成问题/答案
    @Resource
    private PgVectorStore vectorStore;       // PgVectorStore 向量存储
    @Resource
    private TokenTextSplitter tokenTextSplitter;
//    @Resource
//    private FlashcardToolCallback flashcardToolCallback;

    /**
     * 向量检索方法（RAG 检索入口）
     * 功能：
     * 1. 使用 Spring AI 向量存储对输入查询文本进行相似度检索；
     * 2. 根据相似度得分（默认从高到低）排序；
     * 3. 提取检索到的笔记ID和标题，去重，保证同一个笔记只出现一次；
     * 4. 支持可选阈值（similarityThreshold）和返回数量（topK）。
     * @param dto VectorSearchRequestDTO 包含用户ID、查询文本、可选阈值和返回条数
     * @return VectorSearchResponseDTO 包含去重后的笔记ID和标题列表
     * @throws BizException 当查询内容为空时抛出
     */
    @Override
    public VectorSearchResponseDTO vectorSearch(VectorSearchRequestDTO dto) {
        if (dto.getQuery() == null || dto.getQuery().isEmpty()) {
            throw new BizException("没有搜索内容");
        }

        // 1. 构建向量检索请求（Spring AI 风格）
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(dto.getQuery())
                .similarityThreshold(dto.getThreshold() != null ? dto.getThreshold() : 0.75f);

        if (dto.getTopK() != null) {
            builder.topK(dto.getTopK());
        }

        SearchRequest request = builder.build();

        // 2. 执行相似度搜索，结果按相似度得分从高到低排序
        List<Document> docs = vectorStore.similaritySearch(request);

        // 3. 结果为空时直接返回空响应
        if (docs == null || docs.isEmpty()) {
            VectorSearchResponseDTO emptyResponse = new VectorSearchResponseDTO();
            emptyResponse.setNoteInfoList(Collections.emptyList());
            return emptyResponse;
        }

        // 4. 提取 noteId 和 title，并去重（保证每个笔记只出现一次）
        List<VectorSearchResponseDTO.NoteInfo> noteInfoList = new ArrayList<>();
        Set<Long> seenNoteIds = new HashSet<>();

        for (Document doc : docs) {
            Object noteIdObj = doc.getMetadata().get("noteId");
            Object titleObj = doc.getMetadata().get("title");

            if (noteIdObj != null && titleObj != null) {
                Long noteId = Long.valueOf(noteIdObj.toString());
                if (seenNoteIds.add(noteId)) { // 第一次出现才加入
                    VectorSearchResponseDTO.NoteInfo noteInfo = new VectorSearchResponseDTO.NoteInfo();
                    noteInfo.setNoteId(noteId);
                    noteInfo.setTitle(titleObj.toString());
                    noteInfoList.add(noteInfo);
                }
            }
        }

        // 5. 封装响应
        VectorSearchResponseDTO response = new VectorSearchResponseDTO();
        response.setNoteInfoList(noteInfoList);
        return response;
    }
    //    todo 批量要分批，
    @Override
    public FlashCardResponseDTO generateFlashCards(FlashCardRequestDTO dto) {
        // ========== 0. 查询笔记 ==========
        List<NoteVectorDTO> notes = noteService.findNotesByIds(dto.getNoteIds());
        if (notes.isEmpty()) {
            log.warn("未找到指定ID的笔记: {}", dto.getNoteIds());
            throw new BizException("未找到指定ID的笔记");
        }

        // ========== 1. 拼接用户消息 ==========
        StringBuilder messages = new StringBuilder();
        messages.append("请根据以下笔记生成复习卡片(flashcards):\n");
        for (NoteVectorDTO note : notes) {
            messages
                    .append("笔记ID: ").append(note.getNoteId())
                    .append(", 标题: ").append(note.getTitle())
                    .append(", 内容: ").append(note.getContentPlain())
                    .append("\n");
        }

        // ========== 2. 定义 JSON Schema ==========
        Map<String, Object> flashcardItemSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "noteId", Map.of(
                                "type", "integer",
                                "description", "笔记ID，必须与提供的笔记ID匹配"
                        ),
                        "question", Map.of(
                                "type", "string",
                                "description", "基于笔记内容的问题，应简洁明了",
                                "maxLength", 200
                        ),
                        "answer", Map.of(
                                "type", "string",
                                "description", "基于笔记内容的准确答案",
                                "maxLength", 500
                        )
                ),
                "required", List.of("noteId", "question", "answer"),
                "additionalProperties", false
        );

        Map<String, Object> responseSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "flashcards", Map.of(
                                "type", "array",
                                "items", flashcardItemSchema,
                                "description", "生成的复习卡片列表，每个卡片必须对应一个提供的笔记ID"
                        )
                ),
                "required", List.of("flashcards"),
                "additionalProperties", false
        );

        // ========== 3. 创建 ResponseFormat ==========
        ResponseFormat responseFormat = ResponseFormat.builder()
                .type(ResponseFormat.Type.JSON_SCHEMA)
                .jsonSchema(ResponseFormat.JsonSchema.builder()
                        .name("flashcards_response")
                        .schema(responseSchema)
                        .strict(true)
                        .build())
                .build();

        // ========== 4. 设置模型调用参数 ==========
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .responseFormat(responseFormat) // 使用结构化输出
                .model("gpt-4o-mini") // 必须是支持结构化输出的模型
                .temperature(0.1)
                .build();

        // ========== 5. 构建 Prompt ==========
        List<Message> messageList = List.of(
                new SystemMessage("""
            你是一个专业的复习卡片生成助手，请根据用户提供的笔记内容生成结构化的复习卡片。
            
            要求:
            1. 为每个笔记生成至少一个复习卡片
            2. 问题应简洁明了，能够测试对知识点的理解
            3. 答案应准确完整，直接基于提供的笔记内容
            4. 确保每个卡片都与特定的笔记ID关联
            5. 严格按照指定的JSON格式返回数据，不要添加任何额外的解释或说明文字
            """),
                new UserMessage(messages.toString())
        );
        Prompt prompt = new Prompt(messageList, options);

        // ========== 6. 调用模型 ==========
        ChatResponse chatResponse;
        try {
            chatResponse = openAiChatModel.call(prompt);
        } catch (Exception e) {
            log.error("调用AI模型失败", e);
            throw new BizException("调用AI模型失败，请稍后重试");
        }

        // ========== 7. 处理模型返回 ==========
        AssistantMessage output = chatResponse.getResult().getOutput();
        String content = output.getText();

        FlashCardResponseDTO response = new FlashCardResponseDTO();
        Map<Long, FlashCardResponseDTO.FlashCard> flashCardMap = new HashMap<>();
        List<Long> noteIds = new ArrayList<>();
        // 调试代码 - 查看完整的响应结构
        log.debug("完整响应: {}", chatResponse);
        log.debug("结果元数据: {}", chatResponse.getResult().getMetadata());
        log.debug("输出元数据: {}", output.getMetadata());
        // 检查完成原因 - 从 ChatGenerationMetadata 中获取
        ChatGenerationMetadata metadata = chatResponse.getResult().getMetadata();
        String finishReason = metadata.getFinishReason();
        if ("length".equals(finishReason)) {
            log.warn("模型响应因长度限制被截断");
            throw new BizException("响应过长，请减少笔记数量后重试");
        } else if ("content_filter".equals(finishReason)) {
            log.warn("内容被过滤器拦截");
            throw new BizException("内容被安全过滤器拦截，请修改输入后重试");
        }

        // 检查是否有拒绝响应 - 通过检查内容是否包含拒绝相关的关键词
        if (content != null &&
                (content.toLowerCase().contains("sorry") ||
                        content.toLowerCase().contains("cannot") ||
                        content.toLowerCase().contains("unable") ||
                        content.toLowerCase().contains("refuse"))) {
            log.warn("模型可能拒绝生成复习卡片: {}", content);
            throw new BizException("模型拒绝生成复习卡片: " + content);
        }

        // 解析JSON响应
        try {
            JSONObject resultObj = new JSONObject(content);
            JSONArray array = resultObj.getJSONArray("flashcards");

            // 验证和处理卡片
            processFlashcards(array, notes, flashCardMap, noteIds);
        } catch (JSONException e) {
            log.warn("解析复习卡片 JSON 失败: {}", content, e);
            throw new BizException("解析复习卡片 JSON 失败", e);
        }

// ========== 8. 返回结果 ==========
        response.setNoteIds(noteIds);
        response.setFlashCardDTOMap(flashCardMap);
        return response;
    }
    /**
     * 处理并验证从模型返回的复习卡片
     */
    private void processFlashcards(JSONArray rawFlashcards, List<NoteVectorDTO> notes,
                                   Map<Long, FlashCardResponseDTO.FlashCard> flashCardMap,
                                   List<Long> noteIds) {
        Set<Long> processedNoteIds = new HashSet<>();

        for (int i = 0; i < rawFlashcards.length(); i++) {
            try {
                JSONObject obj = rawFlashcards.getJSONObject(i);

                // 验证必需字段
                if (!obj.has("noteId") || !obj.has("question") || !obj.has("answer")) {
                    log.warn("复习卡片缺少必需字段: {}", obj);
                    continue;
                }

                Long noteId = obj.getLong("noteId");
                String question = obj.getString("question")==null?"":obj.getString("question");
                String answer = obj.getString("answer")==null?"":obj.getString("answer");

                // 验证 noteId 是否存在于提供的笔记中
                Optional<NoteVectorDTO> noteOpt = notes.stream()
                        .filter(n -> n.getNoteId().equals(noteId))
                        .findFirst();

                if (noteOpt.isEmpty()) {
                    log.warn("noteId {} 不存在于提供的笔记中", noteId);
                    continue;
                }
                // 检查重复的 noteId
                if (processedNoteIds.contains(noteId)) {
                    log.warn("重复的 noteId: {}", noteId);
                    continue;
                }
                // 验证内容是否为空
                if (question.isEmpty() || answer.isEmpty()) {
                    log.warn("问题或答案为空的复习卡片，noteId: {}", noteId);
                    continue;
                }

                // 构建 FlashCard 对象
                FlashCardResponseDTO.FlashCard card = new FlashCardResponseDTO.FlashCard();
                card.setQuestion(question);
                card.setAnswer(answer);
                card.setTitle(noteOpt.get().getTitle());
                card.setContentMd(noteOpt.get().getContentMd());

                flashCardMap.put(noteId, card);
                noteIds.add(noteId);
                processedNoteIds.add(noteId);

            } catch (JSONException e) {
                log.warn("处理复习卡片时出错: {}", e.getMessage());
            }
        }

        // 检查是否所有笔记都有对应的卡片
        Set<Long> providedNoteIds = notes.stream()
                .map(NoteVectorDTO::getNoteId)
                .collect(Collectors.toSet());

        if (!processedNoteIds.containsAll(providedNoteIds)) {
            log.warn("部分笔记没有生成复习卡片: {}",
                    providedNoteIds.stream()
                            .filter(id -> !processedNoteIds.contains(id))
                            .collect(Collectors.toList()));
        }
    }
    /**
     * 一键生成用户今日复习卡片  todo 待完成
     * @param userId 用户ID
     * @return FlashCardResponseDTO 返回今日所有笔记的复习卡片
     */
    @Override
    public FlashCardResponseDTO generateFlashCards(Long userId) {
//        List<NoteVectorDTO> notes = noteService.findNotesByUserId(userId);
//        List<Long> noteIds = notes.stream().map(NoteVectorDTO::getNoteId).toList();
//        FlashCardRequestDTO dto = new FlashCardRequestDTO();
//        dto.setNoteIds(noteIds);
//        return generateFlashCards(dto);
        return new FlashCardResponseDTO();
    }

    //    todo 根据笔记内容大小，放不同的表，保证不会oom(多个笔记可能会内存爆)
    /**
     * 笔记向量化
     * @param noteIds 笔记ID列表
     * @return
     */
    @Override
    public void vectorizeNote(List<Long> noteIds) {
        if (noteIds == null || noteIds.isEmpty()) return;
        // 查询笔记表的对应内容，分批处理，防止内存占用过高
        //todo 数据库游标 / 流式查询，尤其针对大表场景下批量处理数据
        int batchSize = 20;
        for (int i = 0; i < noteIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, noteIds.size());
            List<Long> batchIds = noteIds.subList(i, end);

            // 1. 查询笔记内容
            List<NoteVectorDTO> noteVectorDTOList = noteService.findNotesByIds(batchIds); // 返回 NoteEntity 列表
            // 1.1 找出在 batchIds 中但没查到的 noteId
            Set<Long> foundNoteIds = noteVectorDTOList.stream()
                    .map(NoteVectorDTO::getNoteId)
                    .collect(Collectors.toSet());
            List<Long> missingNoteIds = batchIds.stream()
                    .filter(id -> !foundNoteIds.contains(id))
                    .toList();
            // 1.2 把找不到的可能情况 1.笔记逻辑删除；2.redis被修改，但是数据库读不到  共有特性：数据库没有对应数据
            // 删除对应的笔记向量，把缓存对应的noteId 移除
            try{
                aiRepository.deleteByNoteIds(missingNoteIds);
            } catch (Exception e) {
                log.error("笔记向量删除失败 missingIds={}", missingNoteIds, e);
            }

            for (NoteVectorDTO noteVectorDTO : noteVectorDTOList) {
                try {
                    Map<String, Object> metadata = new HashMap<>();
                    //todo 以后可以添加标签
                    metadata.put("noteId", noteVectorDTO.getNoteId());
                    metadata.put("userId", noteVectorDTO.getUserId());
                    metadata.put("isDeleted", noteVectorDTO.getIsDeleted());
                    metadata.put("title", noteVectorDTO.getTitle());
                    metadata.put("folderId", noteVectorDTO.getFolderId());
                    // 1) 构建一个 Document（Spring AI 定义的文档对象）
                    Document document = new Document(

                            noteVectorDTO.getContentPlain(),
                            metadata
                    );
                    List<NoteVectorEntity> noteVectorEntityList=new ArrayList<>();
                    // 2) 使用分词器切分
                    List<Document> splits = tokenTextSplitter.apply(List.of(document));
                    //todo 后续可以加 是笔记的第几分片
                    for (Document splitDoc : splits) {
                        if(splitDoc.getText()==null){
                            continue;
                        }
                        // 3) 向量化
                        float[] embedding = embeddingModel.embed(splitDoc.getText());

                        // 4) 保存
                        NoteVectorEntity noteVectorEntity = NoteVectorEntity.builder()
                                .noteId(noteVectorDTO.getNoteId())
                                .userId(noteVectorDTO.getUserId())
                                .folderId(noteVectorDTO.getFolderId())
                                .title(noteVectorDTO.getTitle())
                                .contentPlain(splitDoc.getText()) // 注意保存切分后的片段
                                .isDeleted(noteVectorDTO.getIsDeleted())
                                .deleteTime(noteVectorDTO.getDeleteTime())
                                .version(noteVectorDTO.getVersion())
                                .embedding(embedding)
                                .metadata(metadata)
                                .build();

                        noteVectorEntityList.add(noteVectorEntity);
                    }
                    aiRepository.saveNoteEmbedding(noteVectorEntityList);
                } catch (Exception e) {
                    log.error("笔记向量化失败 noteId={}", noteVectorDTO.getNoteId(), e);
                }
            }
        }
    }
}
