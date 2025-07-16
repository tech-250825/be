package com.ll.demo03.domain.imageTask.service;

import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.domain.imageTask.dto.ImageMessageRequest;
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

    public void sendImageCreationMessage(ImageMessageRequest message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.IMAGE_EXCHANGE,
                RabbitMQConfig.IMAGE_CREATE_ROUTING_KEY,
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