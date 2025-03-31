package com.taco1.demo.service;


import com.taco1.demo.dto.PushTokenDTO;
import com.taco1.demo.entity.PushTokenEntity;
import com.taco1.demo.repository.PushTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class PushTokenNotificationServiceTest {

    @Autowired
    private PushTokenNotificationService pushTokenNotificationService;

    @Autowired
    private PushTokenRepository pushTokenRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    @DisplayName("Test save token")
    @Transactional
    void testSaveToken() {
        // Given
        PushTokenDTO pushTokenDTO = new PushTokenDTO();
        pushTokenDTO.setToken("test-token");
        pushTokenDTO.setDevice("test-device");

        // When
        pushTokenNotificationService.saveToken(pushTokenDTO);

        // Then
        PushTokenEntity savedEntity = pushTokenRepository.findById(pushTokenDTO.getToken()).orElse(null);
        assertNotNull(savedEntity);
        assertEquals(pushTokenDTO.getToken(), savedEntity.getToken());
    }

}
