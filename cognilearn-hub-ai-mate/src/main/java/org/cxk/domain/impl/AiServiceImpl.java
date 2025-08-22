package org.cxk.domain.impl;

import api.INoteService;
import api.dto.NoteVectorDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.cxk.api.dto.FlashCardRequestDTO;
import org.cxk.api.dto.FlashCardResponseDTO;
import org.cxk.api.dto.VectorSearchRequestDTO;
import org.cxk.api.dto.VectorSearchResponseDTO;
import org.cxk.domain.IAiService;
import org.cxk.domain.model.entity.NoteVectorEntity;
import org.cxk.domain.repository.IAiRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
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
    private OpenAiChatModel chatModel;       // Chat 模型，用于生成问题/答案
    @Resource
    private PgVectorStore vectorStore;       // PgVectorStore 向量存储
    @Resource
    private TokenTextSplitter tokenTextSplitter;
    /**
     * 向量检索方法（RAG 检索入口）
     * 向量检索的返回类型通常是 List<Document>
     * Document： String content , Map<String, Object> metadata
     *
     * @param dto VectorSearchRequestDTO 包含查询文本和返回数量
     * @return VectorSearchResponseDTO 包含检索到的文档内容和元数据
     */
    @Override
    public VectorSearchResponseDTO vectorSearch(VectorSearchRequestDTO dto) {
        if (dto.getQuery() == null || dto.getQuery().isEmpty()) {
            return new VectorSearchResponseDTO(Collections.emptyList());
        }

        // 1. 构建向量检索请求
        float[] queryEmbedding = embeddingModel.embed(dto.getQuery());
        SearchRequest request = SearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .topK(dto.getTopK() != null ? dto.getTopK() : 5) // 默认 5 条
                .build();

        // 2. 执行相似度搜索
        List<Document> docs = vectorStore.similaritySearch(request);

        // 3. 封装响应
        return new VectorSearchResponseDTO(
                docs.stream()
                        .map(doc -> new VectorSearchResponseDTO.Doc(
                                doc.getContent(),
                                doc.getMetadata()
                        ))
                        .toList()
        );
    }

    /**
     * 根据给定笔记列表生成复习卡片
     * @param dto FlashCardRequestDTO 包含笔记ID和内容
     * @return FlashCardResponseDTO 返回问题/答案映射
     */
    @Override
    public FlashCardResponseDTO generateFlashCards(FlashCardRequestDTO dto) {
        // 1. 查询笔记向量或内容
        List<NoteVectorDTO> notes = noteService.findNotesByIds(dto.getNoteIds());

        // 2. 用 Chat 模型生成问题/答案
        Map<String, String> qaMap = new HashMap<>();
        for (NoteVectorDTO note : notes) {
            String question = chatModel.generate("生成复习问题: " + note.getContentPlain());
            String answer = chatModel.generate("生成问题的答案: " + note.getContentPlain());
            qaMap.put(question, answer);
        }
        return new FlashCardResponseDTO(qaMap);
    }


    /**
     * 一键生成用户今日复习卡片
     * @param userId 用户ID
     * @return FlashCardResponseDTO 返回今日所有笔记的复习卡片
     */
    @Override
    public FlashCardResponseDTO generateFlashCards(Long userId) {
        List<NoteVectorDTO> notes = noteService.findNotesByUserId(userId);
        List<Long> noteIds = notes.stream().map(NoteVectorDTO::getNoteId).toList();
        FlashCardRequestDTO dto = new FlashCardRequestDTO();
        dto.setNoteIds(noteIds);
        return generateFlashCards(dto);
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
