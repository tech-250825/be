package com.ll.demo03.domain.sse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController {

    private final SseEmitterRepository sseEmitterRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void registerSession(String memberId, String sessionId) {
        redisTemplate.opsForList().rightPush("sse:member:" + memberId, sessionId);
    }

    @GetMapping("/connect")
    public SseEmitter connect(HttpServletRequest request, @RequestParam String memberId) throws IOException {
        String sessionId = request.getSession(true).getId();
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

        sseEmitterRepository.save(sessionId, emitter);

        registerSession(memberId, sessionId);

        emitter.onCompletion(() -> {
            sseEmitterRepository.remove(sessionId);
            redisTemplate.opsForList().remove("sse:member:" + memberId, 1, sessionId);
        });

        emitter.onTimeout(() -> {
            sseEmitterRepository.remove(sessionId);
            redisTemplate.opsForList().remove("sse:member:" + memberId, 1, sessionId);
        });

        String redisImageKey = "notification:image:" + memberId;
        String imageJson = redisTemplate.opsForValue().get(redisImageKey);

        String redisUpscaleKey = "notification:upscale:" + memberId;
        String upscaleJson = redisTemplate.opsForValue().get(redisUpscaleKey);

        String redisVideoKey = "notification:video:" + memberId;
        String videoJson = redisTemplate.opsForValue().get(redisVideoKey);

        emitter.send(SseEmitter.event()
                .data(imageJson)
                .data(upscaleJson)
                .data(videoJson));

        return emitter;
    }

}