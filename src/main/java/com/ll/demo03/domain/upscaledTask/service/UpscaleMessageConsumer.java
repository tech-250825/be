package com.ll.demo03.domain.upscaledTask.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.domain.member.repository.MemberRepository;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import com.ll.demo03.domain.task.dto.AckInfo;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleTaskRequest;
import com.ll.demo03.domain.upscaledTask.entity.UpscaleTask;
import com.ll.demo03.domain.upscaledTask.repository.UpscaleTaskRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@RequiredArgsConstructor
@Slf4j
public class UpscaleMessageConsumer {

    private final UpscaleTaskService upscaleTaskService;
    private final SseEmitterRepository sseEmitterRepository;
    private final MemberRepository memberRepository;
    private final UpscaleTaskRepository upscaleTaskRepository;
    private final Map<String, AckInfo> pendingAcks = new ConcurrentHashMap<>();

    @Value("${custom.webhook-url}")
    private String webhookUrl;

    @RabbitListener(queues = RabbitMQConfig.UPSCALE_QUEUE)
    public void processImageCreation(
            UpscaleTaskRequest message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) {

        try {
            Long memberId = message.getMemberId();
            SseEmitter emitter = sseEmitterRepository.get(String.valueOf(memberId));

            String response = upscaleTaskService.createUpscaleImage(
                    message.getTaskId(),
                    message.getIndex(),
                    webhookUrl + "/api/upscale-images/webhook"
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            String taskId = rootNode.path("data").path("task_id").asText();

            System.out.println("Extracted task_id: " + taskId);

            Map<String, Object> completeData = new HashMap<>();
            completeData.put("status", "업스케일 생성 요청 완료");
            completeData.put("memberId", memberId);
            completeData.put("taskId", taskId);

            emitter.send(SseEmitter.event()
                    .name("status")
                    .data(objectMapper.writeValueAsString(completeData)));

            UpscaleTask upscaleTask = new UpscaleTask();
            upscaleTask.setNewTaskId(taskId);
            upscaleTask.setMember(memberRepository.getOne(memberId));

            upscaleTaskRepository.save(upscaleTask);
            sseEmitterRepository.save(taskId, emitter);

            pendingAcks.put(taskId, new AckInfo(channel, deliveryTag));

            log.info("업스케일 요청 처리 완료: {}", taskId);
        } catch (Exception e) {
            log.error("업스케일 처리 중 오류 발생: {}", e.getMessage(), e);  // 업스케일임을 명확히 함
        }
    }

}
