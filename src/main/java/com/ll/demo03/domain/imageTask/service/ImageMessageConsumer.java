package com.ll.demo03.domain.imageTask.service;

import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.domain.videoTask.dto.VideoMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class ImageMessageConsumer {

    private final ImageTaskService imageTaskService;

    @Value("${custom.webhook-url}")
    private String webhookUrl;

    @RabbitListener(queues = RabbitMQConfig.VIDEO_QUEUE)
    public void processVideoCreation(VideoMessageRequest message) {
        try {
            imageTaskService.processVideoCreationTransactional(message, webhookUrl);
            log.info("영상 생성 요청 처리 완료");
        } catch (Exception e) {
            log.error("영상 생성 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

}
