package com.ll.demo03.domain.task.service;

import com.ll.demo03.domain.task.dto.ImageRequestMessage;
import com.ll.demo03.domain.task.dto.WebhookEvent;
import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleTaskRequestMessage;
import com.ll.demo03.domain.videoTask.dto.VideoMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendImageCreationMessage(ImageRequestMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.IMAGE_EXCHANGE,
                RabbitMQConfig.IMAGE_CREATE_ROUTING_KEY,
                message
        );
    }

    public void sendImageUpscaleMessage(UpscaleTaskRequestMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.UPSCALE_EXCHANGE,
                RabbitMQConfig.UPSCALE_ROUTING_KEY,
                message
        );
    }

    public void sendVideoCreationMessage(VideoMessageRequest message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.VIDEO_EXCHANGE,
                RabbitMQConfig.VIDEO_ROUTING_KEY,
                message
        );
    }
}