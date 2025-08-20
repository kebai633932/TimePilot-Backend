package org.cxk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
//JDK 17+ 和 Spring Boot 3.2+
@Configuration
public class SpringAiConfig {

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

    @Bean
    public OllamaApi ollamaApi(@Value("${spring.ai.ollama.base-url}") String baseUrl) {
        return new OllamaApi(baseUrl);
    }

    @Bean
    public OpenAiApi openAiApi(@Value("${spring.ai.openai.base-url}") String baseUrl, @Value("${spring.ai.openai.api-key}") String apikey) {
        return new OpenAiApi(baseUrl, apikey);
    }

    @Bean
    public SimpleVectorStore vectorStore(@Value("${spring.ai.rag.embed}") String model, OllamaApi ollamaApi, OpenAiApi openAiApi) {
        if ("nomic-embed-text".equalsIgnoreCase(model)) {
            OllamaEmbeddingClient embeddingClient = new OllamaEmbeddingClient(ollamaApi);
            embeddingClient.withDefaultOptions(OllamaOptions.create().withModel("nomic-embed-text"));
            return new SimpleVectorStore(embeddingClient);
        } else {
            OpenAiEmbeddingClient embeddingClient = new OpenAiEmbeddingClient(openAiApi);
            return new SimpleVectorStore(embeddingClient);
        }
    }

    @Bean
    public PgVectorStore pgVectorStore(@Value("${spring.ai.rag.embed}") String model, OllamaApi ollamaApi, OpenAiApi openAiApi, JdbcTemplate jdbcTemplate) {
        if ("nomic-embed-text".equalsIgnoreCase(model)) {
            OllamaEmbeddingClient embeddingClient = new OllamaEmbeddingClient(ollamaApi);
            embeddingClient.withDefaultOptions(OllamaOptions.create().withModel("nomic-embed-text"));
            return new PgVectorStore(jdbcTemplate, embeddingClient);
        } else {
            OpenAiEmbeddingClient embeddingClient = new OpenAiEmbeddingClient(openAiApi);
            return new PgVectorStore(jdbcTemplate, embeddingClient);
        }
    }
}
