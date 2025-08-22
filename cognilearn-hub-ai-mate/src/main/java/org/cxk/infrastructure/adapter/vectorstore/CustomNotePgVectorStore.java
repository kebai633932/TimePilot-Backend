package org.cxk.infrastructure.adapter.vectorstore;

import org.springframework.ai.vectorstore.pgvector.PgVectorStore;

/**
 * @author KJH
 * @description todo 后续优化，可以看一下怎么把向量存储改成自己适合的表
 * @create 2025/8/21 17:01
 */
public class CustomNotePgVectorStore extends PgVectorStore {
    protected CustomNotePgVectorStore(PgVectorStoreBuilder builder) {
        super(builder);
    }

}
