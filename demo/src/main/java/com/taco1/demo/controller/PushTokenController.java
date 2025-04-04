package com.taco1.demo.controller;

import com.taco1.demo.dto.PushTokenDTO;
import com.taco1.demo.entity.PushTokenEntity;
import com.taco1.demo.service.PushTokenNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class PushTokenController {

    private final PushTokenNotificationService pushTokenNotificationService;

    @PostMapping("/save")
    public ResponseEntity<?> savePushToken(@RequestBody PushTokenDTO pushTokenDTO)
    {

        // Save the token to the database
        pushTokenNotificationService.saveToken(pushTokenDTO);

        // Return a success response
        return ResponseEntity.ok("Token saved successfully");
    }

}
