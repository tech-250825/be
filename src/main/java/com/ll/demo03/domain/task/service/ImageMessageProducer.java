package com.ll.demo03.domain.task.service;

import com.ll.demo03.domain.task.dto.ImageRequestMessage;
import com.ll.demo03.domain.task.dto.WebhookEvent;
import com.ll.demo03.config.RabbitMQConfig;
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
}