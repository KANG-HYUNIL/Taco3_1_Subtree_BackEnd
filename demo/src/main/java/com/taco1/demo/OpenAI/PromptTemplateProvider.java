package com.taco1.demo.OpenAI;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class PromptTemplateProvider {

    // 프롬프트 타입 상수 정의
    public static final String DEFAULT = "DEFAULT";
    public static final String TRAVEL = "TRAVEL";
    public static final String EMOTIONAL = "EMOTIONAL";
    public static final String POETIC = "POETIC";

    private final Map<String, String> promptTemplates = new HashMap<>();

    public PromptTemplateProvider() {
        initializeTemplates();
    }

    private void initializeTemplates() {
        // 기본 다이어리 템플릿
        promptTemplates.put(DEFAULT,
                "Write a diary entry based on the following information. " +
                        "Use a friendly and engaging tone.");

        // 여행 일기 템플릿
        promptTemplates.put(TRAVEL,
                "Write a travel diary entry focusing on the locations, sights, and experiences. " +
                        "Highlight the unique aspects of each place and cultural experiences.");

        // 감성적인 일기 템플릿
        promptTemplates.put(EMOTIONAL,
                "Write an emotional and reflective diary entry that focuses on feelings and inner thoughts. " +
                        "Express the emotions that might be associated with these moments.");

        // 시적인 일기 템플릿
        promptTemplates.put(POETIC,
                "Write a poetic diary entry using vivid imagery and metaphors. " +
                        "Create a lyrical narrative that captures the essence of these moments.");
    }

    public String getTemplateByType(String promptType) {
        return promptTemplates.getOrDefault(promptType, promptTemplates.get(DEFAULT));
    }

    public boolean isValidPromptType(String promptType) {
        return promptTemplates.containsKey(promptType);
    }
}