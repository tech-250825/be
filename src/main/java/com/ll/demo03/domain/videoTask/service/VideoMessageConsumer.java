package com.ll.demo03.domain.videoTask.service;

import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.domain.imageTask.service.VideoTaskService;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.repository.MemberRepository;
import com.ll.demo03.domain.taskProcessor.TaskProcessingService;
import com.ll.demo03.domain.videoTask.dto.VideoMessageRequest;
import com.ll.demo03.domain.videoTask.entity.VideoTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class VideoMessageConsumer {

    private final VideoTaskService videoTaskService;

    @Value("${custom.webhook-url}")
    private String webhookUrl;

    @RabbitListener(queues = RabbitMQConfig.VIDEO_QUEUE)
    public void processVideoCreation(VideoMessageRequest message) {
        try {
            videoTaskService.processVideoCreationTransactional(message, webhookUrl);
            log.info("영상 생성 요청 처리 완료");
        } catch (Exception e) {
            log.error("영상 생성 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

}
