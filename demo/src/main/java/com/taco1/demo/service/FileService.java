package com.taco1.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taco1.demo.dto.MetadataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileService {

    private final OpenAIAPIService openAIAPIService;

    private final WebClient webClient;

    //bplip3 API URL

    private String blip3ApiUrl;

    //system prompt
    private String systemPrompt = "";

    private String BodyImage = "image"; // body image 문자열
    private String BodyInput = "input"; // body input 문자열
    private String BodyJopId = "job_id"; // body job id 문자열
    private long MaxAttempt = 10; // 최대 시도 횟수
    private long delayTime = 50000; // 대기 시간 (밀리초 단위)


    // 이미지와 메타데이터를 매핑하여 처리하는 메서드
    public Mono<String> SendImageToModel(
            List<MultipartFile> files,
            List<String> metadataList) {

        if (files.size() != metadataList.size()) {
            return Mono.error(new IllegalArgumentException("파일 수와 메타데이터 수가 일치하지 않습니다."));
        }

        // 결과를 저장할 Map (metadataJson -> 모델 응답)
        Map<String, String> resultMap = new HashMap<>();

        return Flux.range(0, files.size())
                .flatMap(i -> {

                    // 각 파일과 메타데이터를 가져옴
                    MultipartFile file = files.get(i);
                    String metadataJson = metadataList.get(i);


                    return sendImageToBlip3Api(file)
                            .flatMap(jobId -> pollForResult(jobId))
                            .map(result -> {
                                resultMap.put(metadataJson, result);
                                return metadataJson;
                            });
                })
                .then(Mono.defer(() -> {
                    // 모든 결과를 하나의 긴 문자열로 합치기
                    StringBuilder combinedResult = new StringBuilder();
                    for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                        combinedResult.append(entry.getValue()).append("\n"); // 모델 출력
                        combinedResult.append(entry.getKey()).append("\n");  // 메타데이터
                    }

                    // OpenAI API를 통해 결과 처리
                    String openAIResponse = openAIAPIService.generateChatResponse(combinedResult.toString());
                    return Mono.just(openAIResponse);
                }));
    }

    // 이미지를 API로 전송하고 작업 ID 반환
    private Mono<String> sendImageToBlip3Api(MultipartFile imageFile) {
        try {
            // 이미지 파일을 MultipartBodyBuilder로 body에 넣기
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part(BodyImage, imageFile.getBytes())
                    .filename(imageFile.getOriginalFilename());

            bodyBuilder.part(BodyInput, systemPrompt); // system prompt 추가

            //
            return webClient.post()
                    .uri(blip3ApiUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA) // multipart/form-data 설정
                    .bodyValue(bodyBuilder.build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> {
                        try {
                            // JSON 응답을 파싱하여 작업 ID 추출
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode node = mapper.readTree(response);
                            return node.get(BodyJopId).asText(); // 작업 ID 추출

                        } catch (Exception e) {
                            throw new RuntimeException("작업 ID 파싱 실패", e);
                        }
                    });

        } catch (IOException e) {
            return Mono.error(new RuntimeException("이미지 파일 처리 실패", e));
        }
    }


    /*
     * 작업 ID를 기반으로 작업 결과를 폴링하는 메서드
     * 결과가 준비될 때까지 주기적으로 상태를 확인합니다.
     *
     * @param jobId 조회할 작업의 ID
     * @return 작업 결과(문자열)를 포함한 Mono
     */
    // 작업 상태 폴링 메서드 (ModelJobStatus 사용하지 않음)
    public Mono<String> pollForResult(String jobId)
    {
        return Mono.defer(() -> checkJobStatus(jobId)) // 작업 상태 확인
                .repeatWhenEmpty(flux -> flux // 결과가 비어있으면(준비되지 않았으면) 반복
                        .zipWith(Flux.range(1, (int)MaxAttempt)) // 시도 횟수 추적 (1부터 MaxAttempt까지)
                        .flatMap(tuple -> {
                            long attempt = tuple.getT2(); // 현재 시도 횟수
                            if (attempt >= MaxAttempt)
                            { // 최대 시도 횟수 초과 검사
                                return Mono.error(new RuntimeException("최대 시도 횟수 초과"));
                            }
                            return Mono.delay(Duration.ofMillis(delayTime)); // 지정된 시간만큼 대기 후 재시도
                        }));
    }


    // 작업 상태 확인 메서드 (비어있음 - 나중에 구현)
    private Mono<String> checkJobStatus(String jobId) {
        // 이 메서드는 비워둠 - WebClient 또는 RedisTemplate으로 나중에 구현할 예정
        return Mono.empty();
    }


}

