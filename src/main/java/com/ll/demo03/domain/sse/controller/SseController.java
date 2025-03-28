package com.ll.demo03.domain.sse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/sse")
public class SseController {
    private final SseEmitterRepository sseEmitterRepository;
    private final ObjectMapper objectMapper;


    @Autowired
    public SseController(SseEmitterRepository sseEmitterRepository, ObjectMapper objectMapper) {
        this.sseEmitterRepository = sseEmitterRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/{memberId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@PathVariable String memberId) {
        SseEmitter oldEmitter = sseEmitterRepository.get(memberId);
        if (oldEmitter != null) {
            oldEmitter.complete();
            sseEmitterRepository.remove(memberId);
        }

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        sseEmitterRepository.save(memberId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료: {}", memberId);
            sseEmitterRepository.remove(memberId);
        });

        emitter.onTimeout(() -> {
            log.info("SSE 연결 타임아웃: {}", memberId);
            sseEmitterRepository.remove(memberId);
        });

        emitter.onError((e) -> {
            log.error("SSE 연결 오류: {}", memberId, e);
            sseEmitterRepository.remove(memberId);
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("연결이 성공적으로 설정되었습니다."));

            log.info("SSE 연결 설정 완료: {}", memberId);
        } catch (IOException e) {
            log.error("초기 SSE 이벤트 전송 실패: {}", memberId, e);
            sseEmitterRepository.remove(memberId);
        }

        return emitter;
    }
}