package com.taco1.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taco1.demo.dto.MetadataDTO;
import com.taco1.demo.dto.MetadataRequestDTO;
import com.taco1.demo.dto.PushTokenDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class Blip3TaskProcessingService {

    private final RedisService<String> redisService;
    private final OpenAIAPIService openAIAPIService;
    private final ObjectMapper objectMapper;
    private final PushTokenNotificationService pushNotificationService;

    private static final String BLIP_TASK_PREFIX = "blip_task:";
    private static final long POLLING_INTERVAL_MINUTES = 1;
    private static final int MAX_POLLING_ATTEMPTS = 60;


    /**
     * 메타데이터 리스트에서 위치 및 시간 정보 추출
     */
    public List<Map<String, Object>> extractMetadataInfo(List<MetadataDTO> metadataList) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (MetadataDTO metadata : metadataList) {
            Map<String, Object> info = new HashMap<>();

            // 생성 시간 추출
            LocalDateTime creationTime = metadata.getCreationTimeInClientZone();
            if (creationTime != null) {
                info.put("creationTime", creationTime.toString());
            } else {
                // EXIF에서 시간 정보 찾기
                LocalDateTime exifTime = metadata.getExifDateTimeOriginalAsDateTime();
                if (exifTime != null) {
                    info.put("creationTime", exifTime.toString());
                }
            }

            // 위치 정보 추출
            Double latitude = null;
            Double longitude = null;

            // EXIF에서 위경도 확인
            Double exifLat = metadata.getExifLatitude();
            Double exifLon = metadata.getExifLongitude();

            if (exifLat != null && exifLon != null) {
                latitude = exifLat;
                longitude = exifLon;
            } else if (metadata.getLocation() != null) {
                // EXIF에 없으면 location 객체에서 확인
                latitude = metadata.getLocation().getLatitude();
                longitude = metadata.getLocation().getLongitude();
            }

            if (latitude != null && longitude != null) {
                info.put("latitude", latitude);
                info.put("longitude", longitude);
            }

            result.add(info);
        }

        return result;
    }

    /**
     * Redis에서 BLIP 작업 결과를 폴링
     */
    public Mono<JsonNode> pollBlipTaskResult(String taskId) {
        return Mono.defer(() -> {
                    String key = BLIP_TASK_PREFIX + taskId;
                    if (redisService.checkExistsValue(key)) {
                        String value = redisService.getValues(key);
                        try {
                            JsonNode jsonNode = objectMapper.readTree(value);
                            return Mono.just(jsonNode);
                        } catch (JsonProcessingException e) {
                            return Mono.error(new RuntimeException("JSON 파싱 실패", e));
                        }
                    }
                    return Mono.empty();
                })
                .repeatWhenEmpty(flux -> flux
                        .zipWith(Flux.range(1, MAX_POLLING_ATTEMPTS))
                        .flatMap(tuple -> {
                            Integer attempt = tuple.getT2();
                            if (attempt >= MAX_POLLING_ATTEMPTS) {
                                return Mono.error(new RuntimeException("최대 폴링 시도 횟수 초과: " + MAX_POLLING_ATTEMPTS));
                            }
                            log.info("블립 작업 결과 폴링 중... 시도 {}/{}", attempt, MAX_POLLING_ATTEMPTS);
                            return Mono.delay(Duration.ofMinutes(POLLING_INTERVAL_MINUTES));
                        }));
    }


    /**
     * BLIP 작업 결과 처리
     */
    public Mono<List<Map<String, Object>>> processBlipTaskResult(JsonNode resultNode) {

        // 결과 노드가 null인 경우 예외 처리
        if (resultNode == null) {
            return Mono.error(new RuntimeException("결과 데이터가 없습니다"));
        }

        // 작업 상태 확인
        String status = resultNode.path("status").asText();
        if (!"SUCCESS".equals(status)) {
            return Mono.error(new RuntimeException("작업 상태가 성공이 아닙니다: " + status));
        }

        // 결과 노드에서 캡션 정보 추출
        List<Map<String, Object>> captionResults = new ArrayList<>();
        JsonNode resultsNode = resultNode.path("results");

        // 결과 노드가 배열인지 확인
        if (resultsNode.isArray()) {
            for (JsonNode item : resultsNode) {
                Map<String, Object> resultItem = new HashMap<>();
                resultItem.put("index", item.path("index").asInt());
                resultItem.put("caption", item.path("caption").asText());
                captionResults.add(resultItem);
            }
        }

        return Mono.just(captionResults);
    }

    /**
     * 메타데이터와 캡션으로 프롬프트 생성
     */
    private String buildPromptFromData(List<Map<String, Object>> metadataInfo, List<Map<String, Object>> captionResults) {
        StringBuilder prompt = new StringBuilder();

        // 캡션 정보 추가
        prompt.append("이미지 설명:\n");
        for (int i = 0; i < captionResults.size(); i++) {
            prompt.append("- ").append(captionResults.get(i).get("caption")).append("\n");
        }

        // 시간 및 위치 정보 추가
        prompt.append("\n시간 및 위치 정보:\n");
        for (int i = 0; i < Math.min(metadataInfo.size(), captionResults.size()); i++) {
            Map<String, Object> info = metadataInfo.get(i);
            prompt.append("- 이미지 ").append(i + 1).append(": ");

            if (info.containsKey("creationTime")) {
                prompt.append("촬영 시간: ").append(info.get("creationTime")).append(", ");
            }

            if (info.containsKey("latitude") && info.containsKey("longitude")) {
                prompt.append("위치: 위도 ").append(info.get("latitude"))
                        .append(", 경도 ").append(info.get("longitude"));
            }

            prompt.append("\n");
        }

        return prompt.toString();
    }


    /**
     * 메타데이터 요청 전체 처리 흐름
     */
    public Mono<Void> processMetadataRequest(MetadataRequestDTO requestDTO) {
        // 1. 데이터 추출
        String taskId = requestDTO.getTask_id();
        String expoToken = requestDTO.getToken();
        List<MetadataDTO> metadataList = requestDTO.getMetadataDTOList(); // 필드 이름 주의

        log.info("작업 ID: {}, 메타데이터 처리 시작", taskId);

        // 2. 메타데이터에서 시간과 위치 정보 추출
        List<Map<String, Object>> metadataInfo = extractMetadataInfo(metadataList);

        // 3. Blip 작업 결과 폴링 후 처리
        return pollBlipTaskResult(taskId)
                .flatMap(jsonNode -> {

                    // 4. Blip 결과 데이터 처리
                    log.info("Blip 작업 결과 수신. 결과 처리 중...");
                    return processBlipTaskResult(jsonNode)
                            .flatMap(captionResults -> {

                                // 5. OpenAI API 요청을 위한 프롬프트 생성
                                String prompt = buildPromptFromData(metadataInfo, captionResults);
                                log.info("OpenAI API 호출 준비 완료");

                                // 6. OpenAI API 호출 및 결과 처리
                                try {
                                    String aiResponse = openAIAPIService.generateChatResponse(prompt);
                                    log.info("OpenAI 응답 생성 완료");

                                    // 7. Redis에 결과 저장 (원본 taskId를 키로 사용)
                                    redisService.setValues(taskId, aiResponse, Duration.ofHours(24));
                                    log.info("Redis에 결과 저장 완료: {}", taskId);

                                    // 8. 푸시 알림 전송 (PushTokenNotificationService 활용)
                                    if (expoToken != null && !expoToken.isEmpty()) {
                                        return pushNotificationService.sendPushNotification(
                                                expoToken,
                                                "일기 생성 완료",
                                                "일기가 생성되었습니다. 확인해보세요!"
                                        ).then();
                                    }
                                    return Mono.empty();

                                } catch (Exception e) {
                                    return Mono.error(new RuntimeException("AI 응답 처리 중 오류", e));
                                }
                            });
                })
                .onErrorResume(error -> {

                    log.error("작업 처리 중 오류 발생: {}", error.getMessage(), error);
                    return Mono.empty();
                });
    }

}
