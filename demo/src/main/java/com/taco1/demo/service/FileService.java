package com.taco1.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taco1.demo.dto.MetadataDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {


    //이미지가 들어있는 배열을 받아서, 필요 시 전처리 후 모델에게 전송하는 메서드
    public void SendImageToModel(
            List<MultipartFile> files,
            List<String> metadataList)
    {
        ObjectMapper objectMapper = new ObjectMapper();

        if (files.size() != metadataList.size()) {
            throw new IllegalArgumentException("파일 수와 메타데이터 수가 일치하지 않습니다.");
        }

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String metadataJson = metadataList.get(i);

            try {
                // JSON -> DTO 파싱
                MetadataDTO metadata = objectMapper.readValue(metadataJson, MetadataDTO.class);

                //  이후 전처리 또는 모델 전송 로직 작성


                // 예: 모델에 전송 또는 DB 저장 로직
                // modelService.process(file, metadata);

            } 
            catch (Exception e)
            {
                e.printStackTrace();
                System.err.println("메타데이터 파싱 실패 (index: " + i + ") → " + metadataJson);
            }
        }
    }


}