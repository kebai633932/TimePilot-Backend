package org.cxk.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Configuration
public class SpringAiConfig {
    // --- OpenAI API 基础配置 ---
    @Value("${spring.ai.openai.base-url}")
    private String openAiBaseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    // --- Chat 模型配置 ---
    @Value("${spring.ai.openai.chat.options.model}")
    private String chatModel;

    // --- Embedding 配置 ---
    @Value("${spring.ai.openai.embedding.enabled}")
    private boolean embeddingEnabled;

    @Value("${spring.ai.openai.embedding.options.model}")
    private String embeddingModel;

    // --- VectorStore 配置 ---
    @Value("${spring.ai.vectorstore.pgvector.enabled}")
    private boolean pgVectorEnabled;

    /**
     * OpenAI Embedding 向量存储
     */
    @Bean("vectorStore")
    @ConditionalOnBean(name = "pgVectorJdbcTemplate")
    public PgVectorStore pgVectorStore(@Qualifier("pgVectorJdbcTemplate") JdbcTemplate jdbcTemplate,
                                       @Value("${spring.ai.openai.base-url}") String baseUrl,
                                       @Value("${spring.ai.openai.api-key}") String apiKey) {

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        OpenAiEmbeddingModel embeddingModel = new OpenAiEmbeddingModel(openAiApi);

        // PgVectorStore 需要 JdbcTemplate + EmbeddingModel
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .vectorTableName("note_vector")
                .build();
    }
    /**
     * OpenAI 向量化模型 Bean
     */
    @Bean
    public OpenAiEmbeddingModel openAiEmbeddingModel() {

        // OpenAI API 客户端
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(openAiBaseUrl)
                .apiKey(openAiApiKey)
                .build();

        // Chat 配置
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(chatModel)
                .build();

        // 创建 Chat 模型
        return new OpenAiEmbeddingModel(openAiApi);
    }

    /**
     * OpenAI Chat 模型 Bean
     */
    @Bean
    public OpenAiChatModel openAiChatModel(ToolCallingManager toolCallingManager,
                                           RetryTemplate retryTemplate,
                                           ObservationRegistry observationRegistry) {

        // OpenAI API 客户端
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(openAiBaseUrl)
                .apiKey(openAiApiKey)
                .build();

        // Chat 配置
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(chatModel)
                .build();

        // 创建 Chat 模型
        return new OpenAiChatModel(
                openAiApi,
                chatOptions,
                toolCallingManager,
                retryTemplate,
                observationRegistry
        );
    }

    /**
     * 文本切分器
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }
}
