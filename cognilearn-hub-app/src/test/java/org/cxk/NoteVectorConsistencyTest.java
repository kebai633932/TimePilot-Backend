package org.cxk;

import org.cxk.domain.model.entity.NoteVectorEntity;
import org.cxk.domain.repository.IAiRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class NoteVectorConsistencyTest {
//    @MockBean
    @Autowired
    private IAiRepository aiRepository;

    //@MockBean
    @Autowired
    private PgVectorStore pgVectorStore;
    //@MockBean
    @Autowired
    private OpenAiEmbeddingModel embeddingModel;
//    @Test
    // note_vector_test.sql 的数据验证，加数据库加唯一索引失败  ，PgVectorStore 要求id为 uuid
    public void testVectorStorageConsistency() throws Exception {
        // 1. 准备测试数据
        String testContent = "这是一段测试文本内容，用于验证向量存储的一致性。";
        Long noteId = 123L;
        Long userId = 456L;
        Long folderId = 789L;

        // 2. 生成嵌入向量
        float[] embedding = embeddingModel.embed(testContent);

        // 3. 准备元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("noteId", noteId);
        metadata.put("userId", userId);
        metadata.put("folderId", folderId);
        metadata.put("title", "测试标题");

        // 4. 方法1: 使用模型+自定义方法存储
        NoteVectorEntity noteVectorEntity = NoteVectorEntity.builder()
                .noteId(noteId)
                .userId(userId)
                .folderId(folderId)
                .title("测试标题")
                .contentPlain(testContent)
                .isDeleted(false)
                .deleteTime(null)
                .version(0L)
                .embedding(embedding)
                .metadata(metadata)
                .build();
        List<NoteVectorEntity> noteVectorEntityList = new ArrayList<>();
        noteVectorEntityList.add(noteVectorEntity);

        // 使用自定义repository保存,成功
        aiRepository.saveNoteEmbedding(noteVectorEntityList);

        // 5. 方法2: 直接使用PgVectorStore存储相同的文档
        Document document = new Document(testContent, metadata);
        pgVectorStore.add(List.of(document));

        // 6. 验证两种方法存储的数据是否一致
        // 这里可以添加验证逻辑，比较两种存储方式的结果
    }
}