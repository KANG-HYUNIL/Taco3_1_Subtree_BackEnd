package com.taco1.demo.service;


import com.taco1.demo.dto.PushTokenDTO;
import com.taco1.demo.entity.PushTokenEntity;
import com.taco1.demo.message.ExpoPushMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class PushTokenNotificationService {

    private final RedisExpoTokenService redisExpoTokenService;
    private final WebClient webClient;

    //EXPO 서버 URL
    private final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    //알림에 실어 보낼 TITLE 및 MESSAGE
    private final String TITLE = "title";
    private final String MESSAGE = "message";

    private final String DIARY_TITLE = "Diary Title";
    private final String DIARY_MESSAGE = "Diary Message";


    // Save the token to Redis
    public void saveToken(PushTokenDTO pushTokenDTO) {
        try {
            // Expo Token을 Redis에 저장
            redisExpoTokenService.addToken(pushTokenDTO.getToken());
        } catch (Exception e) {
            throw new RuntimeException("Error saving token", e);
        }
    }

    //특정 사용자에게 token을 통해 PUSH 알림 보내기 메소드
    public Mono<String> sendPushNotification(String token, String title, String body)
    {
        //Message 생성
        ExpoPushMessage expoPushMessage = new ExpoPushMessage(token, title, body);

        try
        {
            return webClient.post()
                    .uri(EXPO_PUSH_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Accept", "application/json")
                    .bodyValue(List.of(expoPushMessage))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(new RuntimeException(
                                            "푸시 알림 전송 실패: 상태코드=" + clientResponse.statusCode() + ", 응답=" + errorBody))))
                    .bodyToMono(String.class)
                    .onErrorResume(e -> Mono.error(new RuntimeException("푸시 알림 전송 중 예외 발생: " + e.getMessage(), e)));

        }
        //예외 처리
        catch (Exception e)
        {
            throw new RuntimeException("푸시 알림 전송 중 예외 발생: " + e.getMessage(), e);
        }
    }

    // 모든 사용자에게 PUSH 메소드 실행 (논블로킹)
    public Mono<Void> sendToAllUsers() {
        // Redis에서 모든 토큰 가져오기
        Set<String> tokens = redisExpoTokenService.getAllTokens();

        // 모든 token에게 병렬로 발송
        return Flux.fromIterable(tokens)
                .flatMap(token -> sendPushNotification(token, TITLE, MESSAGE))
                .then();
    }

    @Scheduled(cron = "0 0 21 * * *")  // 매일 오후 9?시에 실행
    public void schedulePushToAll() {
        sendToAllUsers().block();
    }

}
