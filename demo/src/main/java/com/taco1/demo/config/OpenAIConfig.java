package com.taco1.demo.config;


import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.model}")
    private String model;

    @Value("${openai.api.max-tokens}")
    private Integer maxTokens;

    @Value("${openai.api.temperature}")
    private Double temperature;

    @Bean
    public OpenAIClient openAiApi() {
        String cleanApiKey = apiKey != null ?
                apiKey.trim().replace("\n", "").replace("\r", "") :
                "";
        return OpenAIOkHttpClient.builder()
                .apiKey(cleanApiKey)
                .build();
    }
}
