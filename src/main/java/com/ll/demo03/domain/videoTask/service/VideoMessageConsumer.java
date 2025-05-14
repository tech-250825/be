package com.ll.demo03.domain.videoTask.service;

import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.domain.taskProcessor.TaskProcessingService;
import com.ll.demo03.domain.videoTask.dto.VideoMessageRequest;
import com.ll.demo03.domain.videoTask.entity.VideoTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class VideoMessageConsumer {

    private final VideoTaskService videoTaskService;
    private final TaskProcessingService taskProcessingService;

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

            String taskId = taskProcessingService.extractTaskIdFromResponse(response);

            VideoTask task=videoTaskService.saveVideoTask(taskId, memberId);

            taskProcessingService.sendVideoSseEvent(memberId, task);

            log.info("영상 생성 요청 처리 완료: {}", taskId);

        } catch (Exception e) {
            log.error("영상 생성 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
