package com.ll.demo03.global.infrastructure;

import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.global.port.MessageProducer;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import com.ll.demo03.videoTask.controller.request.I2VQueueRequest;
import com.ll.demo03.videoTask.controller.request.T2VQueueRequest;
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
    public void sendFaceDetailerCreationMessage(ImageQueueRequest message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.FACE_DETAILER_EXCHANGE,
                RabbitMQConfig.FACE_DETAILER_CREATE_ROUTING_KEY,
                message
        );
    }

    @Override
    public void sendCreationMessage(T2VQueueRequest message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.T2V_EXCHANGE,
                RabbitMQConfig.T2V_ROUTING_KEY,
                message
        );
    }

    @Override
    public void sendCreationMessage(I2VQueueRequest message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.I2V_EXCHANGE,
                RabbitMQConfig.I2V_ROUTING_KEY,
                message
        );
    }

    @Override
    public void sendLastFrameMessage(I2VQueueRequest message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DOWNLOAD_EXCHANGE,
                RabbitMQConfig.DOWNLOAD_ROUTING_KEY,
                message
        );
    }
}