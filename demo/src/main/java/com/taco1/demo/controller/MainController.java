package com.taco1.demo.controller;

import com.taco1.demo.dto.DiaryDTO;
import com.taco1.demo.dto.MetadataRequestDTO;
import com.taco1.demo.service.FileService;
import com.taco1.demo.service.OpenAIAPIService;
import com.taco1.demo.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class MainController {

    private final FileService fileService;
    private final RedisService<String> redisService;
    private final OpenAIAPIService openAIAPIService;

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
                        diaryDTO.setContent(value);

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
    public ResponseEntity<String> receiveMetadata(@RequestBody MetadataRequestDTO requestDTO) {
        // 여기서 requestDTO에는 메타데이터 배열, task_id, token이 포함됨
        // 구현은 추후 작성 예정

        return ResponseEntity.ok("메타데이터 수신 완료");
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

