package com.ll.demo03.imageTask.infrastructure;

import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.imageTask.controller.port.I2ITaskService;
import com.ll.demo03.imageTask.controller.request.I2ITask.I2IQueueRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class I2IMessageConsumer {
    private final I2ITaskService i2ITaskService;

    @RabbitListener(queues = RabbitMQConfig.I2I_QUEUE)
    public void processCreation(I2IQueueRequest message) {
        try {
            i2ITaskService.processCreationTransactional(message);
            log.info("I2I 생성 요청 처리 완료");
        } catch (Exception e) {
            log.error("I2I 생성 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
