package com.ll.demo03.domain.task.service;

import com.ll.demo03.domain.task.dto.ImageRequestMessage;
import com.ll.demo03.domain.taskProcessor.TaskProcessingService;
import com.ll.demo03.domain.task.entity.Task;
import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.domain.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageMessageConsumer {

    private final TaskService taskService;
    private final TaskProcessingService taskProcessingService;
    private final TaskRepository taskRepository;

    @RabbitListener(queues = RabbitMQConfig.IMAGE_QUEUE)
    public void processImageCreation(ImageRequestMessage message) {
        try {
            Long memberId = message.getMemberId();

            String response = taskService.createImage(
                    message.getGptPrompt(),
                    message.getRatio(),
                    message.getCref(),
                    message.getSref(),
                    message.getWebhookUrl()
            );

            String taskId = taskProcessingService.extractTaskIdFromResponse(response);

            saveImageTask(memberId, taskId, message);

            taskProcessingService.sendSseStatusEvent(memberId, taskId, "이미지 생성 요청 완료");

        } catch (Exception e) {
            log.error("이미지 생성 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Transactional
    private void saveImageTask(Long memberId, String taskId, ImageRequestMessage message) {
        try {
            Task task = new Task();
            task.setTaskId(taskId);
            task.setRawPrompt(message.getRawPrompt());
            task.setGptPrompt(message.getGptPrompt());
            task.setRatio(message.getRatio());
            task.setMember(taskProcessingService.getMember(memberId));

            taskRepository.save(task);
            log.info("이미지 작업 저장 완료: {}", taskId);
        } catch (Exception e) {
            log.error("이미지 작업 저장 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}