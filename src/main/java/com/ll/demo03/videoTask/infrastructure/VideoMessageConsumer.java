package com.ll.demo03.videoTask.infrastructure;

import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.videoTask.controller.port.VideoTaskService;
import com.ll.demo03.videoTask.controller.request.I2VQueueRequest;
import com.ll.demo03.videoTask.controller.request.T2VQueueRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class VideoMessageConsumer {

    private final VideoTaskService videoTaskService;

    @RabbitListener(queues = RabbitMQConfig.T2V_QUEUE)
    public void processCreation(T2VQueueRequest message) {
        try {
            videoTaskService.process(message);
            log.info("영상 생성 요청 처리 완료");
        } catch (Exception e) {
            log.error("영상 생성 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.I2V_QUEUE)
    public void processCreation(I2VQueueRequest message) {
        try {
            videoTaskService.process(message);
            log.info("영상 생성 요청 처리 완료");
        } catch (Exception e) {
            log.error("영상 생성 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

}
