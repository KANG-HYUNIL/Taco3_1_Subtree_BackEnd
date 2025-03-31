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

import java.awt.*;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
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
    @MockitoBean
    private FileService fileService;

    @BeforeEach
    void setUp() {
        // MockMvc 설정이 필요하다면 여기에 작성
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
                "multipartFile", // 파라미터 이름
                "test1.jpg", // 원본 파일 이름
                MediaType.IMAGE_JPEG_VALUE, // 컨텐츠 타입
                "test1-content".getBytes(StandardCharsets.UTF_8) // 파일 내용
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "multipartFile", // 파라미터 이름
                "test2.jpg", // 원본 파일 이름
                MediaType.IMAGE_JPEG_VALUE, // 컨텐츠 타입
                "test2-content".getBytes(StandardCharsets.UTF_8) // 파일 내용
        );

        //when, then
        mockMvc.perform(
                multipart("/api/image") // 요청 URL
                        .file(file1)
                        .file(file2)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("multipartFile", "test")
        ).andExpect(status().isOk()); // 응답 상태 코드 확인;
//                .andExpect(content().string(containsString("test"))); // 응답 본문 확인

    }


}
