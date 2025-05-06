package com.taco1.demo.OpenAI;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.taco1.demo.service.PromptTemplateProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service //Service 도 Bean 등록임
@Primary // @Primary 어노테이션을 사용하여 이 구현체를 기본으로 설정
public class OpenAIProviderImpl implements OpenAIProvider{

    private final OpenAIClient openAIClient;
    private final PromptTemplateProvider promptTemplateProvider;

    // System 프롬프트
    private final String systemMessage =
            "Hi, I am a helpful assistant. Write a diary with these descriptions. " +
                    "Please write a diary entry based on the following information. " +
                    "You can use the information to create a diary entry. " +
                    "Please write in a friendly and engaging manner.";

    // Temp
    private final Double temperature = 0.7;

    // Max Token
    private final Integer maxTokens = 5000;


    @Autowired
    public OpenAIProviderImpl(OpenAIClient openAIClient, PromptTemplateProvider promptTemplateProvider) {
        this.openAIClient = openAIClient;
        this.promptTemplateProvider = promptTemplateProvider;
    }



    /*
     * OpenAI의 Chat API를 호출하여 응답을 가져옵니다.
     *
     * @param prompt 사용자 입력 프롬프트
     * @return AI의 응답 텍스트
     */
    @Override
    public String generateResponse(String prompt) {

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

    @Override
    public String generateResponse(String prompt, String promptType, String customPrompt) {
        // 기본 구현은 mixed 방식 사용
        return generateResponseMixed(prompt, promptType, customPrompt);
    }

    @Override
    public String generateResponseAllInSystem(String prompt, String promptType, String customPrompt) {
        // 프롬프트 타입에 해당하는 템플릿 가져오기
        String typeTemplate = promptTemplateProvider.getTemplateByType(promptType);

        // 시스템 메시지 구성
        StringBuilder systemPrompt = new StringBuilder(systemMessage);
        systemPrompt.append("\n\n").append(typeTemplate);

        // 커스텀 프롬프트가 있으면 추가
        if (customPrompt != null && !customPrompt.isEmpty()) {
            systemPrompt.append("\n\n").append(customPrompt);
        }

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O)
                .addSystemMessage(systemPrompt.toString())
                .addUserMessage(prompt)
                .temperature(temperature)
                .maxCompletionTokens(maxTokens)
                .build();

        ChatCompletion result = openAIClient.chat().completions().create(params);

        return result.choices().stream().flatMap(choice -> choice.message().content().stream())
                .collect(Collectors.joining(" "));
    }

    @Override
    public String generateResponseMixed(String prompt, String promptType, String customPrompt) {
        // 프롬프트 타입에 해당하는 템플릿 가져오기
        String typeTemplate = promptTemplateProvider.getTemplateByType(promptType);

        // 시스템 메시지에는 기본 + 타입 템플릿
        StringBuilder systemPrompt = new StringBuilder(systemMessage);
        systemPrompt.append("\n\n").append(typeTemplate);

        // 유저 메시지에는 원본 프롬프트 + 커스텀 프롬프트
        StringBuilder userPrompt = new StringBuilder(prompt);
        if (customPrompt != null && !customPrompt.isEmpty()) {
            userPrompt.append("\n\nAdditional instructions: ").append(customPrompt);
        }

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O)
                .addSystemMessage(systemPrompt.toString())
                .addUserMessage(userPrompt.toString())
                .temperature(temperature)
                .maxCompletionTokens(maxTokens)
                .build();

        ChatCompletion result = openAIClient.chat().completions().create(params);

        return result.choices().stream().flatMap(choice -> choice.message().content().stream())
                .collect(Collectors.joining(" "));
    }

    @Override
    public String generateResponseAllInUser(String prompt, String promptType, String customPrompt) {
        // 프롬프트 타입에 해당하는 템플릿 가져오기
        String typeTemplate = promptTemplateProvider.getTemplateByType(promptType);

        // 유저 메시지에 모든 프롬프트 통합
        StringBuilder userPrompt = new StringBuilder(prompt);
        userPrompt.append("\n\nStyle guide: ").append(typeTemplate);

        if (customPrompt != null && !customPrompt.isEmpty()) {
            userPrompt.append("\n\nAdditional instructions: ").append(customPrompt);
        }

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O)
                .addSystemMessage(systemMessage)
                .addUserMessage(userPrompt.toString())
                .temperature(temperature)
                .maxCompletionTokens(maxTokens)
                .build();

        ChatCompletion result = openAIClient.chat().completions().create(params);

        return result.choices().stream().flatMap(choice -> choice.message().content().stream())
                .collect(Collectors.joining(" "));
    }

}
