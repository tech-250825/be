package com.ll.demo03.webhook;

import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.global.controller.request.WebhookEvent;
import com.ll.demo03.global.domain.Status;
import com.ll.demo03.global.port.RedisService;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.member.domain.Member;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ImageWebhookProcessorImpl implements WebhookProcessor<WebhookEvent> {

    private final ImageTaskRepository taskRepository;
    private final UGCRepository UGCRepository;
    private final RedisService redisService;

    public void processWebhookEvent(WebhookEvent event) {
        Long taskId = event.getTaskId();
        String status = event.getStatus();
        String runpodId = event.getRunpodId();

        try {
            switch (status){
                case "FAILED" -> handleFailed(taskId,  runpodId);
                case "COMPLETED" -> handleCompleted(taskId, event,  runpodId);
                default -> handleFailed(taskId, runpodId);
            }
        } catch (Exception e) {
            log.error("웹훅 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
            handleFailed(taskId, runpodId);
        }
    }

    public void saveToDatabase(Long taskId, String url) {
        try {

            ImageTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Image task not found"));

            UGC ugc = UGC.ofImage(url, task);
            UGCRepository.save(ugc);

            log.info("✅ DB 저장 완료: taskId={}, imageUrl={}", taskId, ugc);
        } catch (Exception e) {
            log.error("DB 저장 중 오류 발생: {}", e.getMessage(), e);
        }
    }


    public void handleFailed(Long taskId, String runpodId) {
        try {
            ImageTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Image task not found"));

            task = task.updateStatus(Status.FAILED, runpodId);

            Member member = task.getCreator();
            member.increaseCredit( 1);
            Long memberId = member.getId();

            redisService.publishNotificationToOtherServers(memberId, taskId, "", "이미지 생성에 실패했습니다.");
            redisService.removeFromQueue("image", taskId);
            taskRepository.save(task);
        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void handleCompleted(Long taskId, WebhookEvent event, String runpodId) {
        try {
            ImageTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Image task not found"));

            task = task.updateStatus(Status.COMPLETED, runpodId);

            Long memberId = task.getCreator().getId();
            String url = event.getImages();
            String prompt = event.getPrompt();

            saveToDatabase(task.getId(), url);

            redisService.publishNotificationToOtherServers(memberId, taskId, url, prompt);
            redisService.removeFromQueue("image", taskId);
            taskRepository.save(task);
        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

}