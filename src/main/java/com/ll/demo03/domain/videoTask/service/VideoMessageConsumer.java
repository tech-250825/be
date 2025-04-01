package com.ll.demo03.domain.videoTask.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.domain.member.repository.MemberRepository;
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
    private final SseEmitterRepository sseEmitterRepository;
    private final MemberRepository memberRepository;
    private final VideoTaskRepository videoTaskRepository;
    private final Map<String, AckInfo> pendingAcks = new ConcurrentHashMap<>();

    @Value("${custom.webhook-url}")
    private String webhookUrl;

    @RabbitListener(queues = RabbitMQConfig.VIDEO_QUEUE)
    public void processVideoCreation(
            VideoMessageRequest message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) {
        try {
            Long memberId = message.getMemberId();
            SseEmitter emitter = sseEmitterRepository.get(String.valueOf(memberId));

            if (emitter == null) {
                log.warn("SSE Emitter가 없음. 새로 생성합니다. memberId: {}", memberId);
                emitter = new SseEmitter(60 * 1000L); // 60초 타임아웃
                sseEmitterRepository.save(String.valueOf(memberId), emitter);
            }

            String response = videoTaskService.createVideo(
                    message.getImageUrl(),
                    message.getPrompt(),
                    webhookUrl + "/api/videos/webhook"
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            String taskId = rootNode.path("data").path("task_id").asText();


            Map<String, Object> completeData = new HashMap<>();
            completeData.put("status", "동영상 생성 요청 완료");
            completeData.put("memberId", memberId);
            completeData.put("taskId", taskId);

            emitter.send(SseEmitter.event()
                    .name("status")
                    .data(objectMapper.writeValueAsString(completeData)));

            VideoTask videoTask = new VideoTask();
            videoTask.setTaskId(taskId);
            videoTask.setMember(memberRepository.getOne(memberId));

            videoTaskRepository.save(videoTask);
            sseEmitterRepository.save(taskId, emitter);

            pendingAcks.put(taskId, new AckInfo(channel, deliveryTag));

            log.info("영상 생성 요청 처리 완료: {}", taskId);
        } catch (Exception e) {
            log.error("영상 생성 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

}
