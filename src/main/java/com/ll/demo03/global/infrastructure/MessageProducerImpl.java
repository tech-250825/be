package com.ll.demo03.global.infrastructure;

import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.global.port.MessageProducer;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducerImpl implements MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendImageCreationMessage(ImageQueueRequest message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.IMAGE_EXCHANGE,
                RabbitMQConfig.IMAGE_CREATE_ROUTING_KEY,
                message
        );
    }

    @Override
    public void sendVideoCreationMessage(com.ll.demo03.videoTask.controller.request.VideoQueueRequest message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.VIDEO_EXCHANGE,
                RabbitMQConfig.VIDEO_ROUTING_KEY,
                message
        );
    }
}