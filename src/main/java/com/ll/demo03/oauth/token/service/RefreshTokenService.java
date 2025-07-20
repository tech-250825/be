package com.ll.demo03.oauth.token.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-token.expire-time}")
    private long refreshTokenExpireTime;

    public RefreshTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(
                email + ":refresh-token",
                refreshToken,
                refreshTokenExpireTime,
                TimeUnit.MILLISECONDS
        );
    }

    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(email + ":refresh-token");
    }

}