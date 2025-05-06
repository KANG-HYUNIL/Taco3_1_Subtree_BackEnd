package com.taco1.demo.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class MetadataRequestDTO {
    private List<MetadataDTO> metadataDTOList; // 기존 MetadataDTO 클래스 활용
    private String task_id;
    private String token;
    private String promptType;    // 추가: 프롬프트 타입
    private String customPrompt;  // 추가: 사용자 정의 프롬프트
}