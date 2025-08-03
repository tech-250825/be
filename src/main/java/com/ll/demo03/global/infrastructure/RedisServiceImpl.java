package com.ll.demo03.global.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.global.port.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void setValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void pushToQueue(String type, Long taskId){
        switch(type){
            case "image" -> redisTemplate.opsForList().rightPush("image:queue", String.valueOf(taskId));
            case "t2v" -> redisTemplate.opsForList().rightPush("t2v:queue", String.valueOf(taskId));
            case "i2v" -> redisTemplate.opsForList().rightPush("i2v:queue", String.valueOf(taskId));
        }
    }

    @Override
    public void removeFromQueue(String type, Long taskId){
        switch(type){
            case "image" -> redisTemplate.opsForList().remove("image:queue", 1,  String.valueOf(taskId));
            case "t2v" -> redisTemplate.opsForList().remove("t2v:queue", 1,  String.valueOf(taskId));
            case "i2v" -> redisTemplate.opsForList().remove("i2v:queue", 1,  String.valueOf(taskId));
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) //requires_new로 분리 트랜잭션
    public void publishNotificationToOtherServers(Long memberId, Long taskId, String prompt, String url ) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", "video",
                    "memberId", memberId,
                    "taskId", taskId,
                    "prompt", prompt,
                    "imageUrl", url
            );
            String jsonMessage = objectMapper.writeValueAsString(payload);
            redisTemplate.convertAndSend("sse-notification-channel", jsonMessage);
            log.debug("✅ Redis Publish 성공: memberId={}, taskId={}", memberId, taskId);
        } catch (Exception e) {
            log.error("❌ Redis Publish 실패: memberId={}, taskId={}, error={}", memberId, taskId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) //requires_new로 분리 트랜잭션
    public void publishNotificationToOtherServers(Long memberId, Long boardId, Long taskId, String prompt, String url ) {
        try {
            Map<String, Object> payload = Map.of(
                    "type", "board",
                    "boardId", boardId,
                    "memberId", memberId,
                    "taskId", taskId,
                    "prompt", prompt,
                    "imageUrl", url
            );
            String jsonMessage = objectMapper.writeValueAsString(payload);
            redisTemplate.convertAndSend("sse-notification-channel", jsonMessage);
            log.debug("✅ Redis Publish 성공: memberId={}, taskId={}", memberId, taskId);
        } catch (Exception e) {
            log.error("❌ Redis Publish 실패: memberId={}, taskId={}, error={}", memberId, taskId, e.getMessage(), e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) //requires_new로 분리 트랜잭션
    public void publishNotificationToOtherServers(Long memberId, Long taskId, String prompt, List<String> url) {
        try {
            Map<String, Object> payload = Map.of(
                    "type" , "image",
                    "memberId", memberId,
                    "taskId", taskId,
                    "prompt", prompt,
                    "imageUrl", url
            );
            String jsonMessage = objectMapper.writeValueAsString(payload);
            redisTemplate.convertAndSend("sse-notification-channel", jsonMessage);
            log.debug("✅ Redis Publish 성공: memberId={}, taskId={}", memberId, taskId);
        } catch (Exception e) {
            log.error("❌ Redis Publish 실패: memberId={}, taskId={}, error={}", memberId, taskId, e.getMessage(), e);
        }
    }
}
