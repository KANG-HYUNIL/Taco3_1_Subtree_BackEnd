package com.taco1.demo.service;

import com.taco1.demo.OpenAI.OpenAIProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OpenAIAPIServiceTest {

    @Mock // 순수한 Mockito의 가상 객체 생성. 테스트 클래스 전용 사용 용도
    private OpenAIProvider openAIProvider;

    // 가상 객체는 동일하나, 동일 클래스 내의 다른 모든 Mock 객체를 주입받게 하는 용
    @InjectMocks
    private OpenAIAPIService openAIAPIService;

    @BeforeEach
    void setUp() {
        // OpenAIProvider mock 객체의 동작 정의
        when(openAIProvider.generateResponse(anyString())).thenReturn("테스트 응답");
    }

    @Test
    void testGenerateChatResponse() {
        // given
        String prompt = "테스트 프롬프트";

        // when
        String response = openAIAPIService.generateChatResponse(prompt);

        // then
        assertEquals("테스트 응답", response);
        verify(openAIProvider).generateResponse(prompt);
    }
}
