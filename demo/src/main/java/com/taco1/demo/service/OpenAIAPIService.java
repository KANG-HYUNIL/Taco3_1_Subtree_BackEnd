package com.taco1.demo.service;


import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import com.taco1.demo.OpenAI.OpenAIProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


@Service
public class OpenAIAPIService {

    private final OpenAIProvider openAIProvider;

    @Autowired
    public OpenAIAPIService(OpenAIProvider openAIProvider) {
        this.openAIProvider = openAIProvider;
    }

    public String generateChatResponse(String prompt) {
        return openAIProvider.generateResponse(prompt);
    }


    // 새로운 메소드들
    public String generateChatResponse(String prompt, String promptType, String customPrompt) {
        return openAIProvider.generateResponse(prompt, promptType, customPrompt);
    }

    public String generateChatResponseAllInSystem(String prompt, String promptType, String customPrompt) {
        return openAIProvider.generateResponseAllInSystem(prompt, promptType, customPrompt);
    }

    public String generateChatResponseMixed(String prompt, String promptType, String customPrompt) {
        return openAIProvider.generateResponseMixed(prompt, promptType, customPrompt);
    }

    public String generateChatResponseAllInUser(String prompt, String promptType, String customPrompt) {
        return openAIProvider.generateResponseAllInUser(prompt, promptType, customPrompt);
    }

}
