package com.taco1.demo.config;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class AwsCredentialsConfig {
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @PostConstruct
    public void init() {
        System.setProperty("aws.accessKeyId", accessKey);
        System.setProperty("aws.secretKey", secretKey);
        System.setProperty("aws.region", region);
    }
}
