package com.ll.demo03.domain.upscaledTask.service;

import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.task.entity.Task;
import com.ll.demo03.domain.task.repository.TaskRepository;
import com.ll.demo03.domain.taskProcessor.TaskProcessingService;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleTaskRequest;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleTaskRequestMessage;
import com.ll.demo03.domain.upscaledTask.entity.UpscaleTask;
import com.ll.demo03.domain.upscaledTask.repository.UpscaleTaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpscaleMessageConsumer {

    private final UpscaleTaskService upscaleTaskService;
    private final TaskProcessingService taskProcessingService;
    private final UpscaleTaskRepository upscaleTaskRepository;
    private final TaskRepository taskRepository;

    @Value("${custom.webhook-url}")
    private String webhookUrl;

    @RabbitListener(queues = RabbitMQConfig.UPSCALE_QUEUE)
    public void processImageCreation(UpscaleTaskRequestMessage message) {
        try {
            Long memberId = message.getMemberId();

            String response = upscaleTaskService.createUpscaleImage(
                    message.getTaskId(),
                    message.getIndex(),
                    webhookUrl + "/api/upscale-images/webhook"
            );

            String taskId = taskProcessingService.extractTaskIdFromResponse(response);
            log.info("Extracted upscale task_id: {}", taskId);

            saveUpscaleTask(taskId, message);

            taskProcessingService.sendSseStatusEvent(memberId, taskId, "업스케일 생성 요청 완료");

        } catch (Exception e) {
            log.error("업스케일 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Transactional
    private void saveUpscaleTask(String taskId, UpscaleTaskRequestMessage message) {
        Member member = taskProcessingService.getMember(message.getMemberId());

        Task task = taskRepository.findByTaskId(message.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        UpscaleTask upscaleTask = new UpscaleTask();
        upscaleTask.setImageIndex(message.getIndex());
        upscaleTask.setTask(task);
        upscaleTask.setNewTaskId(taskId);
        upscaleTask.setMember(member);

        upscaleTaskRepository.save(upscaleTask);
    }
}
