package com.taco1.demo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taco1.demo.OpenAI.OpenAIResponseParser;
import com.taco1.demo.dto.DiaryDTO;
import com.taco1.demo.dto.MetadataDTO;
import com.taco1.demo.dto.MetadataRequestDTO;
import com.taco1.demo.service.Blip3TaskProcessingService;
import com.taco1.demo.service.FileService;
import com.taco1.demo.service.OpenAIAPIService;
import com.taco1.demo.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final FileService fileService;
    private final RedisService<String> redisService;
    private final OpenAIAPIService openAIAPIService;
    private final Blip3TaskProcessingService blip3TaskProcessingService;
    private final TaskExecutor taskExecutor;
    private final OpenAIResponseParser openAIResponseParser;

    //Content를 받아 ResponseEntity<DiaryDTO>로 변환하는 메서드
    private static ResponseEntity<DiaryDTO> apply(String content) {
        DiaryDTO diaryDTO = new DiaryDTO();
        diaryDTO.setContent(content);
        return ResponseEntity.ok(diaryDTO);
    }

//    @PostMapping("/api/image")
//    @ResponseBody
//    // 반환값이 ResponseEntity<DiaryDTO>로 되어 있어서 , Spring WebFlux는 비동기를 대기함
//    public Mono<ResponseEntity<DiaryDTO>> test(
//            @RequestPart("image") List<MultipartFile> files,
//            @RequestPart("metadata") List<String> metadataList) {
//
//        return fileService.SendImageToModel(files, metadataList) // Service 단 비동기 시작
//                .map(MainController::apply) // 작업 완료 대기 후 DiaryDTO 로 변환
//                .onErrorResume(e -> Mono.just( // 에러 발생 시 처리
//                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                                .body(new DiaryDTO())
//                ));
//    }

    /**
     * Redis에 저장된 작업 상태 확인 API
     *
     * @param taskId 확인할 작업의 고유 식별자
     * @return 작업 상태 또는 결과
     */
    @GetMapping("/api/redis/check/{taskId}")
    public Mono<ResponseEntity<DiaryDTO>> checkRedisData(@PathVariable String taskId) {
        // RedisService를 사용하여 taskId로 데이터 조회
        return Mono.fromCallable(() -> {
                    if (redisService.checkExistsValue(taskId)) {
                        String value = redisService.getValues(taskId);
                        DiaryDTO diaryDTO = new DiaryDTO();
                        openAIResponseParser.parseGptResponse(value, diaryDTO);

                        return ResponseEntity.ok(diaryDTO);
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new DiaryDTO()); // 빈 DiaryDTO 반환
                    }
                })
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new DiaryDTO()) // 에러 시 빈 DiaryDTO 반환
                ));
    }

    /**
     * 메타데이터와 작업 정보를 받는 API 엔드포인트
     */
    @PostMapping("/api/metadata")
    public ResponseEntity<String> receiveMetadata(@RequestParam("metadata") String metadataJson) {
        try {
            // JSON 문자열을 DTO로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(metadataJson);

            // 프론트엔드 형식을 백엔드 DTO에 매핑
            MetadataRequestDTO requestDTO = new MetadataRequestDTO();
            requestDTO.setMetadataDTOList(objectMapper.convertValue(rootNode.path("images"),
                    new TypeReference<List<MetadataDTO>>() {}));
            requestDTO.setTask_id(rootNode.path("task_id").asText());
            requestDTO.setToken(rootNode.path("token").asText());

            // 비동기 처리
            // 즉시 응답 반환
            taskExecutor.execute(() -> {
                blip3TaskProcessingService.processMetadataRequest(requestDTO)
                        .subscribe(/* 콜백 */);
            });

            return ResponseEntity.ok("메타데이터 수신 완료");
        } catch (Exception e) {
            log.error("메타데이터 파싱 오류", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("메타데이터 형식 오류");
        }
    }

    /**
     * test api
     */
    @PostMapping("/test/gpt")
    public ResponseEntity<String> testGPT(
            @RequestBody String content) {

        String res = openAIAPIService.generateChatResponse(content);

        return ResponseEntity.ok(res);
    }

}

