package com.taco1.demo.service;

import com.taco1.demo.converter.PushTokenConverter;
import com.taco1.demo.dto.PushTokenDTO;
import com.taco1.demo.entity.PushTokenEntity;
import com.taco1.demo.message.ExpoPushMessage;
import com.taco1.demo.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PushTokenNotificationService {

    private final PushTokenRepository pushTokenRepository;
    private final RestTemplate restTemplate;

    //EXPO 서버 URL
    private final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    //알림에 실어 보낼 TITLE 및 MESSAGE
    private final String TITLE = "";
    private final String MESSAGE = "";


    // Save the token to the database
    public void saveToken(PushTokenDTO pushTokenDTO)
    {

        try {
            // Expo Token 저장
            PushTokenEntity pushTokenEntity = PushTokenConverter.toEntity(pushTokenDTO);
            pushTokenRepository.save(pushTokenEntity);

        } catch (Exception e) {
            // Handle exception
            throw new RuntimeException("Error saving token", e);
        }
    }

    //특정 사용자에게 token을 통해 PUSH 알림 보내기 메소드
    public void sendPushNotification(String token, String title, String body)
    {
        //Message 생성
        ExpoPushMessage expoPushMessage = new ExpoPushMessage(token, title, body);

        //HttpHeaders 생성 및 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");

        //요청 생성
        HttpEntity<List<ExpoPushMessage>> request = new HttpEntity<>(List.of(expoPushMessage), headers);

        try
        {
            //통신 발사
            ResponseEntity<String> response = restTemplate.postForEntity(EXPO_PUSH_URL, request, String.class);

            //상태코드 확인
            if (!response.getStatusCode().is2xxSuccessful())
            {
                throw new RuntimeException("푸시 알림 전송 실패: 상태코드=" + response.getStatusCode()
                        + ", 응답=" + response.getBody());
            }
        }
        //예외 처리
        catch (Exception e)
        {
            throw new RuntimeException("푸시 알림 전송 중 예외 발생: " + e.getMessage(), e);
        }
    }

    //모든 사용자에게 PUSH 메소드 실행
    public void sendToAllUsers() {

        //가지고 있는 모든 token 획득
        List<PushTokenEntity> tokens = pushTokenRepository.findAll();

        //모든 token에게 하나씩 발송
        for (PushTokenEntity token : tokens) {
            sendPushNotification(token.getToken(), TITLE, MESSAGE);
        }
    }

    @Scheduled(cron = "0 0 21 * * *")  // 매일 오후 9?시에 실행
    public void schedulePushToAll() {
        sendToAllUsers();
    }

}
