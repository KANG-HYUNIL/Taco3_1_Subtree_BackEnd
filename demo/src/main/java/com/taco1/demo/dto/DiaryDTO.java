package com.taco1.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

//일기 데이터 전송을 위한 DTO
//test2
@Getter
@Setter
public class DiaryDTO {

    private String content;

    private String date;

    private String topImageUri;

    private List<String> imageUris;


}
