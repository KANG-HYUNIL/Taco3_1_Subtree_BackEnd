package com.taco1.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisExpoTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String DEFAULT_TOKEN_KEY = "expo:push:tokens";

    /**
     * 토큰을 지정된 키의 Set에 추가
     */
    public void addToken(String token) {
        redisTemplate.opsForSet().add(DEFAULT_TOKEN_KEY, token);
    }

    /**
     * 토큰을 사용자 지정 키의 Set에 추가
     */
    public void addToken(String key, String token) {
        redisTemplate.opsForSet().add(key, token);
    }

    /**
     * 토큰 삭제
     */
    public void removeToken(String token) {
        redisTemplate.opsForSet().remove(DEFAULT_TOKEN_KEY, token);
    }

    /**
     * 사용자 지정 키에서 토큰 삭제
     */
    public void removeToken(String key, String token) {
        redisTemplate.opsForSet().remove(key, token);
    }

    /**
     * 기본 키에 저장된 모든 토큰 조회
     */
    public Set<String> getAllTokens() {
        Set<String> tokens = redisTemplate.opsForSet().members(DEFAULT_TOKEN_KEY);
        return tokens != null ? tokens : new HashSet<>();
    }

    /**
     * 사용자 지정 키에 저장된 모든 토큰 조회
     */
    public Set<String> getAllTokens(String key) {
        Set<String> tokens = redisTemplate.opsForSet().members(key);
        return tokens != null ? tokens : new HashSet<>();
    }

    /**
     * 기본 키에 특정 토큰이 존재하는지 확인
     */
    public boolean containsToken(String token) {
        Boolean exists = redisTemplate.opsForSet().isMember(DEFAULT_TOKEN_KEY, token);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 사용자 지정 키에 특정 토큰이 존재하는지 확인
     */
    public boolean containsToken(String key, String token) {
        Boolean exists = redisTemplate.opsForSet().isMember(key, token);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 기본 키에 저장된 토큰 수 조회
     */
    public long countTokens() {
        Long size = redisTemplate.opsForSet().size(DEFAULT_TOKEN_KEY);
        return size != null ? size : 0;
    }

    /**
     * 사용자 지정 키에 저장된 토큰 수 조회
     */
    public long countTokens(String key) {
        Long size = redisTemplate.opsForSet().size(key);
        return size != null ? size : 0;
    }
}