package com.taco1.demo.service;

import com.taco1.demo.dto.PushTokenDTO;
import com.taco1.demo.entity.PushTokenEntity;
import com.taco1.demo.repository.PushTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // 통합 테스트 대신 Mockito 확장 사용
public class PushTokenNotificationServiceTest {

    @Mock // 실제 객체 대신 가짜 객체 사용
    private PushTokenRepository pushTokenRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks // 모든 Mock 객체가 주입된 서비스 생성
    private PushTokenNotificationService pushTokenNotificationService;

    @Captor
    private ArgumentCaptor<PushTokenEntity> entityCaptor;

    @Test
    @DisplayName("토큰 저장 테스트")
    void testSaveToken() {
        // Given
        PushTokenDTO pushTokenDTO = new PushTokenDTO();
        pushTokenDTO.setToken("test-token");
        pushTokenDTO.setDevice("test-device");

        // When
        pushTokenNotificationService.saveToken(pushTokenDTO);

        // Then
        verify(pushTokenRepository).save(entityCaptor.capture());
        PushTokenEntity capturedEntity = entityCaptor.getValue();

        assertEquals(pushTokenDTO.getToken(), capturedEntity.getToken());
        assertEquals(pushTokenDTO.getDevice(), capturedEntity.getDevice());
    }

    @Test
    @DisplayName("푸시 알림 전송 테스트")
    void testSendPushNotification() {
        // Given
        String token = "test-token";
        String title = "Test Title";
        String body = "Test Body";
        String expectedResponse = "Success";

        // WebClient Mock 설정
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedResponse));

        // When, Then
        StepVerifier.create(pushTokenNotificationService.sendPushNotification(token, title, body))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    @DisplayName("모든 사용자에게 푸시 알림 전송 테스트")
    void testSendToAllUsers() {
        // Given
        PushTokenEntity entity = new PushTokenEntity();
        entity.setToken("test-token");

        when(pushTokenRepository.findAll()).thenReturn(Collections.singletonList(entity));

        // WebClient Mock 설정 (위 테스트와 동일)
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("Success"));

        // When, Then
        StepVerifier.create(pushTokenNotificationService.sendToAllUsers())
                .verifyComplete();

        // findAll이 호출되었는지 확인
        verify(pushTokenRepository).findAll();
    }
}