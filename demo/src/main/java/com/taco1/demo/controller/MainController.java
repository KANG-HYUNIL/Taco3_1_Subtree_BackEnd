package com.taco1.demo.controller;

import com.taco1.demo.dto.DiaryDTO;
import com.taco1.demo.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class MainController {

    private final FileService fileService;

    //Content를 받아 ResponseEntity<DiaryDTO>로 변환하는 메서드
    private static ResponseEntity<DiaryDTO> apply(String content) {
        DiaryDTO diaryDTO = new DiaryDTO();
        diaryDTO.setContent(content);
        return ResponseEntity.ok(diaryDTO);
    }

    @PostMapping("/api/image")
    @ResponseBody
    // 반환값이 ResponseEntity<DiaryDTO>로 되어 있어서 , Spring WebFlux는 비동기를 대기함
    public Mono<ResponseEntity<DiaryDTO>> test(
            @RequestPart("image") List<MultipartFile> files,
            @RequestPart("metadata") List<String> metadataList) {

        return fileService.SendImageToModel(files, metadataList) // Service 단 비동기 시작
                .map(MainController::apply) // 작업 완료 대기 후 DiaryDTO 로 변환
                .onErrorResume(e -> Mono.just( // 에러 발생 시 처리
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new DiaryDTO())
                ));


    }



}

