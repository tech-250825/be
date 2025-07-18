package com.ll.demo03.imageTask.infrastructure;

import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.imageTask.domain.ImageTaskInitiate;
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

    @RabbitListener(queues = RabbitMQConfig.IMAGE_QUEUE)
    public void processImageCreation(ImageTaskInitiate message) {
        try {
            imageTaskService.processImageCreationTransactional(message);
            log.info("영상 생성 요청 처리 완료");
        } catch (Exception e) {
            log.error("영상 생성 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

}
