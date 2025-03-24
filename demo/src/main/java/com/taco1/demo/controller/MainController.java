package com.taco1.demo.controller;

import com.taco1.demo.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class MainController {

    private final FileService fileService;

    @PostMapping("/api/image")
    @ResponseBody
    public ResponseEntity<?> test(@RequestPart("multipartFile") List<MultipartFile> files) {

        try {
            fileService.SendImageToModel(files); //이미지 파일들 처리 서비스 호출

            //모든 처리 작업 완료 후 데이터를 다시 응답에 돌려줘야 함, fixme
            return new ResponseEntity<>("test", HttpStatus.OK);
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }


    }



}

