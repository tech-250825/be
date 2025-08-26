package com.ll.demo03.imageTask.infrastructure;

import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import com.ll.demo03.imageTask.controller.request.ImageQueueV3Request;
import com.ll.demo03.imageTask.service.ImageTaskServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class ImageMessageConsumer {

    private final ImageTaskServiceImpl imageTaskService;

    @RabbitListener(queues = RabbitMQConfig.FACE_DETAILER_QUEUE)
    public void processFaceDetailerCreation(ImageQueueRequest message) {
        try {
            imageTaskService.processImageCreationFaceDetailer(message);
            log.info("영상 생성 요청 처리 완료");
        } catch (Exception e) {
            log.error("영상 생성 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.PLAIN_IMAGE_QUEUE)
    public void processPlainCreation(ImageQueueV3Request message) {
        try {
            imageTaskService.processPlainCreationTransactional(message);
            log.info("영상 생성 요청 처리 완료");
        } catch (Exception e) {
            log.error("영상 생성 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }


}
