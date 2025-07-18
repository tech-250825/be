package com.ll.demo03.global.infrastructure;

import com.ll.demo03.global.port.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redisTemplate;

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
    public void pushToImageQueue(String taskId){
        redisTemplate.opsForList().rightPush("image:queue", String.valueOf(taskId));
    }
}
