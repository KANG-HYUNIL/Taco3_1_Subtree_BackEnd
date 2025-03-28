package com.taco1.demo.message;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExpoPushMessage
{
    @JsonProperty("to")
    private String to;

    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

}
