package com.ll.demo03.domain.task.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.member.repository.MemberRepository;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import com.ll.demo03.domain.task.dto.AckInfo;
import com.ll.demo03.domain.task.dto.ImageRequestMessage;
import com.rabbitmq.client.Channel;
import com.ll.demo03.domain.task.entity.Task;
import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.domain.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.messaging.handler.annotation.Header;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageMessageConsumer {

    private final TaskService taskService;
    private final SseEmitterRepository sseEmitterRepository;
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;

    @RabbitListener(queues = RabbitMQConfig.IMAGE_QUEUE)
    public void processImageCreation(
            ImageRequestMessage message
    ) {
        try {
            Long memberId = message.getMemberId();
            SseEmitter emitter = sseEmitterRepository.get(String.valueOf(memberId));

            String response = taskService.createImage(
                    message.getGptPrompt(),
                    message.getRatio(),
                    message.getCref(),
                    message.getSref(),
                    message.getWebhookUrl()
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            String taskId = rootNode.path("data").path("task_id").asText();

            Task task = new Task();
            task.setTaskId(taskId);
            task.setRawPrompt(message.getRawPrompt());
            task.setGptPrompt(message.getGptPrompt());
            task.setRatio(message.getRatio());
            task.setMember(memberRepository.getOne(memberId));

            taskRepository.save(task);

            Map<String, Object> completeData = new HashMap<>();
            completeData.put("status", "이미지 생성 요청 완료");
            completeData.put("memberId", memberId);
            completeData.put("taskId", taskId);

            emitter.send(SseEmitter.event()
                    .name("status")
                    .data(objectMapper.writeValueAsString(completeData)));

            log.info("이미지 생성 요청 처리 완료: {}", taskId);
        } catch (Exception e) {
            log.error("이미지 생성 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}