package com.ll.demo03.domain.videoTask.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.domain.member.repository.MemberRepository;
import com.ll.demo03.domain.notification.service.NotificationService;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import com.ll.demo03.domain.task.dto.AckInfo;
import com.ll.demo03.domain.videoTask.dto.VideoMessageRequest;
import com.ll.demo03.domain.videoTask.dto.VideoTaskRequest;
import com.ll.demo03.domain.videoTask.entity.VideoTask;
import com.ll.demo03.domain.videoTask.repository.VideoTaskRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoMessageConsumer {

    private final VideoTaskService videoTaskService;
    private final NotificationService notificationService;

    @Value("${custom.webhook-url}")
    private String webhookUrl;

    @RabbitListener(queues = RabbitMQConfig.VIDEO_QUEUE)
    public void processVideoCreation(
            VideoMessageRequest message
    ) {
        try {
            Long memberId = message.getMemberId();

            String response = videoTaskService.createVideo(
                    message.getImageUrl(),
                    message.getPrompt(),
                    webhookUrl + "/api/videos/webhook"
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            String taskId = rootNode.path("data").path("task_id").asText();

            videoTaskService.saveVideoTask(taskId, memberId);

            notificationService.sendNotification(memberId, taskId);

            log.info("영상 생성 요청 처리 완료: {}", taskId);

        } catch (Exception e) {
            log.error("영상 생성 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
