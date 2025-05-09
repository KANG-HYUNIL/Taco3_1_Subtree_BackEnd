package com.taco1.demo.OpenAI;

import com.taco1.demo.dto.DiaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class OpenAIResponseParser {



    /**
     * GPT 응답을 파싱하여 본문과 요약을 분리
     * @param gptResponse GPT의 전체 응답 텍스트
     * @param diaryDTO 결과를 저장할 DTO 객체
     */
    public void parseGptResponse(String gptResponse, DiaryDTO diaryDTO) {
        // 태그 기반 파싱 - 제안한 프롬프트 형식에 따라 [DIARY]와 [SUMMARY] 태그 찾기
        String content = "";
        String summary = "";

        // 형식화된 출력에서 [DIARY]와 [/DIARY] 태그 사이의 콘텐츠 추출
        Pattern diaryPattern = Pattern.compile("\\[DIARY](.*?)\\[/DIARY]", Pattern.DOTALL);
        Matcher diaryMatcher = diaryPattern.matcher(gptResponse);
        if (diaryMatcher.find()) {
            content = diaryMatcher.group(1).trim();
        } else {
            // 태그가 없으면 전체 텍스트를 콘텐츠로 간주
            content = gptResponse.trim();
        }

        // [SUMMARY]와 [/SUMMARY] 태그 사이의 요약 추출
        Pattern summaryPattern = Pattern.compile("\\[SUMMARY](.*?)\\[/SUMMARY]", Pattern.DOTALL);
        Matcher summaryMatcher = summaryPattern.matcher(gptResponse);
        if (summaryMatcher.find()) {
            summary = summaryMatcher.group(1).trim();
        }

        // DTO에 설정
        diaryDTO.setContent(content);
        diaryDTO.setSummary(summary);

//        // 로깅
//        log.info("Content successfully parsed: {} chars", content.length());
//        log.info("Summary successfully parsed: {} chars", summary.length());
    }


}
