package com.taco1.demo.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PushTokenDTO {

    private String token;

    private String device;

}
