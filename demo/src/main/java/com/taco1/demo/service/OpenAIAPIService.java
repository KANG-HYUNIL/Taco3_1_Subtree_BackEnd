package com.taco1.demo.service;


import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Service
public class OpenAIAPIService {

    private final OpenAIClient openAIClient;

    // System 프롬프트
    private final String systemMessage =
            "";

    // Temp
    private final Double temperature = 0.7;

    // Max Token
    private final Integer maxTokens = 5000;

    /**
     * OpenAI의 Chat API를 호출하여 응답을 가져옵니다.
     * 
     * @param prompt 사용자 입력 프롬프트
     * @return AI의 응답 텍스트
     */
    public String generateChatResponse(String prompt) {

        //api 요청의 파라미터 설정
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O)
                .addSystemMessage(systemMessage)
                .addUserMessage(prompt)
                .temperature(temperature)
                .maxCompletionTokens(maxTokens)
                .build();

        //응답 받기
        ChatCompletion result = openAIClient.chat().completions().create(params);

        // 모든 choice와 content 조각을 모아서 하나의 긴 문자열로 반환
        String response = result.choices().stream().flatMap(choice -> choice.message().content().stream())
                .collect(Collectors.joining(" "));

        // 응답을 반환
        return response;
    }


}
