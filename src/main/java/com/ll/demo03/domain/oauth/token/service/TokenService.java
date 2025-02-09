package com.ll.demo03.domain.oauth.token.service;

import com.ll.demo03.domain.oauth.token.TokenProvider;
import com.ll.demo03.domain.oauth.token.entity.Token;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-token.expire-time}")
    private long refreshTokenExpireTime;

    public TokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveOrUpdate(String username, String refreshToken, String accessToken) {
        // Redis에 토큰 저장
        // refreshToken을 저장하고, 해당 사용자의 토큰과 연결
        System.out.println(refreshTokenExpireTime);
        redisTemplate.opsForValue().set(username + ":refresh-token", refreshToken, refreshTokenExpireTime);
        redisTemplate.opsForValue().set(username + ":access-token", accessToken);
    }

    public void deleteToken(String username) {
        // Redis에서 토큰 삭제
        redisTemplate.delete(username + ":refresh-token");
        redisTemplate.delete(username + ":access-token");
    }
}