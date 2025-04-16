package com.taco1.demo.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService<T> {

    //RedisTemplate 객체 생성 및 관리
    @Autowired
    private final RedisTemplate<String, T> redisTemplate;

    //Redis에 값 저장
    public void setValues(String key, T value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    //Redis에서 값 획득
    public T getValues(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    //Redis에 값이 존재하는지 확인
    public boolean checkExistsValue(String key) {
        return redisTemplate.hasKey(key);
    }

    //Redis에 값 삭제
    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

}
