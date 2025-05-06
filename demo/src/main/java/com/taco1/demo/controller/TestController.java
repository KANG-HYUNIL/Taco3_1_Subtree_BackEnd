package com.taco1.demo.controller;

import com.taco1.demo.service.OpenAIAPIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test/api")
@RequiredArgsConstructor
public class TestController {

    private final OpenAIAPIService openAIAPIService;

    /**
     * 기본 GPT 응답 테스트
     */
    @PostMapping("/gpt/basic")
    public ResponseEntity<String> testBasicGPT(@RequestBody String content) {
        String response = openAIAPIService.generateChatResponse(content);
        return ResponseEntity.ok(response);
    }

    /**
     * 프롬프트 타입과 사용자 프롬프트 함께 테스트 (기본 설정)
     */
    @PostMapping("/gpt/prompt")
    public ResponseEntity<String> testPromptGPT(
            @RequestBody String content,
            @RequestParam String promptType,
            @RequestParam(required = false) String customPrompt) {

        String response = openAIAPIService.generateChatResponse(content, promptType, customPrompt);
        return ResponseEntity.ok(response);
    }

    /**
     * 모든 프롬프트를 시스템 메시지에 넣는 방식 테스트
     */
    @PostMapping("/gpt/system")
    public ResponseEntity<String> testSystemGPT(
            @RequestBody String content,
            @RequestParam String promptType,
            @RequestParam(required = false) String customPrompt) {

        String response = openAIAPIService.generateChatResponseAllInSystem(content, promptType, customPrompt);
        return ResponseEntity.ok(response);
    }

    /**
     * 타입 템플릿은 시스템에, 사용자 프롬프트는 유저 메시지에 넣는 방식 테스트
     */
    @PostMapping("/gpt/mixed")
    public ResponseEntity<String> testMixedGPT(
            @RequestBody String content,
            @RequestParam String promptType,
            @RequestParam(required = false) String customPrompt) {

        String response = openAIAPIService.generateChatResponseMixed(content, promptType, customPrompt);
        return ResponseEntity.ok(response);
    }

    /**
     * 모든 프롬프트를 유저 메시지에 넣는 방식 테스트
     */
    @PostMapping("/gpt/user")
    public ResponseEntity<String> testUserGPT(
            @RequestBody String content,
            @RequestParam String promptType,
            @RequestParam(required = false) String customPrompt) {

        String response = openAIAPIService.generateChatResponseAllInUser(content, promptType, customPrompt);
        return ResponseEntity.ok(response);
    }
}