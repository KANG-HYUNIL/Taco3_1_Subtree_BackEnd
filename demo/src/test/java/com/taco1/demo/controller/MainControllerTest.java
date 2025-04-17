package com.taco1.demo.controller;


import com.taco1.demo.service.FileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = MainController.class)
public class MainControllerTest {

    // Controller test 위한 MockMvc 객체
    @Autowired
    private MockMvc mockMvc;

    // MockitoBean으로, 실 객체 말고 임시? 객체 활용해 테스트
    //스프링 통합 테스트용 가상 객체 Annotation, WebMvcTest나 SpringBootTest 사용 용도
    @MockitoBean
    private FileService fileService;

    private final String Image = "image"; // body image 문자열
    private final String Metadata = "metadata"; // body metadata 문자열

    @BeforeEach
    void setUp() {
        // MockMvc 설정이 필요하다면 여기에 작성
        // FileService의 SendImageToModel 메서드가 호출될 때 "test"를 반환하도록 설정
        when(fileService.SendImageToModel(any(java.util.List.class), any(List.class)))
                .thenReturn(Mono.just("test"));
    }

    @AfterEach
    void tearDown() {
        // MockMvc 설정 해제 필요하다면 여기에 작성
    }


    @Test
    @DisplayName("여러 파일이 multipartFile 리스트에 담겨 한 요청에 전송되는지 확인")
    void testSendImages() throws Exception {

        //given
        MockMultipartFile file1 = new MockMultipartFile(
                Image, // 파라미터 이름
                "test1.jpg", // 원본 파일 이름
                MediaType.IMAGE_JPEG_VALUE, // 컨텐츠 타입
                "test1-content".getBytes(StandardCharsets.UTF_8) // 파일 내용
        );

        MockMultipartFile file2 = new MockMultipartFile(
                Image, // 파라미터 이름
                "test2.jpg", // 원본 파일 이름
                MediaType.IMAGE_JPEG_VALUE, // 컨텐츠 타입
                "test2-content".getBytes(StandardCharsets.UTF_8) // 파일 내용
        );


        MockMultipartFile metadata1 = new MockMultipartFile(
                Metadata, // 메타데이터 파라미터 이름
                "",
                MediaType.TEXT_PLAIN_VALUE,
                "{\"test\":\"metadata1\"}".getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile metadata2 = new MockMultipartFile(
                Metadata, // 동일한 이름으로 여러 메타데이터 추가
                "",
                MediaType.TEXT_PLAIN_VALUE,
                "{\"test\":\"metadata2\"}".getBytes(StandardCharsets.UTF_8)
        );


        // when, then
        mockMvc.perform(
                        multipart("/api/image")
                                .file(file1)
                                .file(file2)
                                .file(metadata1)
                                .file(metadata2)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("test"));

        // 서비스 메서드가 정확히 1번 호출되었는지 검증
        verify(fileService, times(1)).SendImageToModel(any(List.class), any(List.class));
    }


}
