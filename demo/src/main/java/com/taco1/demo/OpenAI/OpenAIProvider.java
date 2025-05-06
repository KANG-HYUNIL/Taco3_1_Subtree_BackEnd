package com.taco1.demo.OpenAI;

public interface OpenAIProvider {
    // 기존 메소드
    String generateResponse(String prompt);

    // 새로운 메소드들
    String generateResponse(String prompt, String promptType, String customPrompt);

    // 모든 프롬프트를 시스템 메시지에 넣는 버전
    String generateResponseAllInSystem(String prompt, String promptType, String customPrompt);

    // 타입 템플릿은 시스템에, 사용자 프롬프트는 유저 메시지에 넣는 버전
    String generateResponseMixed(String prompt, String promptType, String customPrompt);

    // 모든 프롬프트를 유저 메시지에 넣는 버전
    String generateResponseAllInUser(String prompt, String promptType, String customPrompt);
}