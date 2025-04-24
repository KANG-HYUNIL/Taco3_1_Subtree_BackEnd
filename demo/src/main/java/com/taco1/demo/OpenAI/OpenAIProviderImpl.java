package com.taco1.demo.OpenAI;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service //Service 도 Bean 등록임
@Primary // @Primary 어노테이션을 사용하여 이 구현체를 기본으로 설정
public class OpenAIProviderImpl implements OpenAIProvider{

    private final OpenAIClient openAIClient;

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
    public OpenAIProviderImpl(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
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
}
