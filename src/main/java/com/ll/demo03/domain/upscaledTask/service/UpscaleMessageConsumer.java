package com.ll.demo03.domain.upscaledTask.service;

import com.ll.demo03.config.RabbitMQConfig;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.repository.MemberRepository;
import com.ll.demo03.domain.imageTask.entity.ImageTask;
import com.ll.demo03.domain.imageTask.repository.ImageTaskRepository;
import com.ll.demo03.domain.taskProcessor.TaskProcessingService;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleTaskRequestMessage;
import com.ll.demo03.domain.upscaledTask.entity.UpscaleTask;
import com.ll.demo03.domain.upscaledTask.repository.UpscaleTaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpscaleMessageConsumer {

    private final UpscaleTaskService upscaleTaskService;
    private final ImageRepository imageRepository;
    private final TaskProcessingService taskProcessingService;
    private final UpscaleTaskRepository upscaleTaskRepository;
    private final ImageTaskRepository imageTaskRepository;
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.UPSCALE_QUEUE)
    public void processImageCreation(UpscaleTaskRequestMessage message) {
        try {
            Long memberId = message.getMemberId();

            String response = upscaleTaskService.createUpscaleImage(
                    message.getTaskId(),
                    message.getIndex(),
                    message.getWebhookUrl()
            );
            log.info("업스케일 이미지 생성 요청 응답: {}", response);

            String taskId = taskProcessingService.extractTaskIdFromResponse(response);
            redisTemplate.opsForList().rightPush("upscale:queue", taskId);

            Image image = imageRepository.getByTaskIdAndIndex(message.getTaskId(), Integer.valueOf(message.getIndex()))
                    .orElseThrow(() -> new RuntimeException("Image not found for taskId: " + message.getTaskId() + ", index: " + message.getIndex()));
            image.setIsUpscaled(true);
            imageRepository.save(image);

            Member member = memberRepository.getById(memberId);
            int credit = member.getCredit();
            credit -= 1;
            member.setCredit(credit);
            memberRepository.save(member);

            log.info("Extracted upscale task_id: {}", taskId);

            UpscaleTask task = saveUpscaleTask(taskId, message);

            taskProcessingService.sendUpscaleSseEvent(memberId, task);

        } catch (Exception e) {
            log.error("업스케일 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private UpscaleTask saveUpscaleTask(String taskId, UpscaleTaskRequestMessage message) {
        Member member = taskProcessingService.getMember(message.getMemberId());

        ImageTask imageTask = imageTaskRepository.findByTaskId(message.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        UpscaleTask upscaleTask = new UpscaleTask();
        upscaleTask.setImageIndex(message.getIndex());
        upscaleTask.setImageTask(imageTask);
        upscaleTask.setNewTaskId(taskId);
        upscaleTask.setMember(member);

        UpscaleTask result =upscaleTaskRepository.save(upscaleTask);
        return result;
    }
}
