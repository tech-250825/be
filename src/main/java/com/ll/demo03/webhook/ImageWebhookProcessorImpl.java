package com.ll.demo03.webhook;

import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.global.controller.request.ImageWebhookEvent;
import com.ll.demo03.global.domain.Status;
import com.ll.demo03.global.port.AlertService;
import com.ll.demo03.global.port.RedisService;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
import com.ll.demo03.webhook.port.ImageWebhookProcessor;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ImageWebhookProcessorImpl implements ImageWebhookProcessor<ImageWebhookEvent> {

    private final ImageTaskRepository taskRepository;
    private final UGCRepository UGCRepository;
    private final RedisService redisService;
    private final MemberRepository memberRepository;
    private final AlertService alertService;

    public void processWebhookEvent(ImageWebhookEvent event) {
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

    public void saveToDatabase(Long taskId, List<String> urls) {
        try {
            ImageTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Image task not found"));

            for (String url : urls) {
                UGC ugc = UGC.ofImage(url, task , urls.indexOf(url));
                UGCRepository.save(ugc);
                log.info("✅ DB 저장 완료: taskId={}, imageUrl={}", taskId, url);
            }

        } catch (Exception e) {
            log.error("DB 저장 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void handleFailed(Long taskId, String runpodId) {
        try {
            ImageTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Image task not found"));

            task = task.updateStatus(Status.FAILED, runpodId);
            alertService.sendAlert("이미지 생성에 실패했습니다" + taskId);

            Member member = task.getCreator();
            member.increaseCredit( task.getResolutionProfile().getBaseCreditCost());
            Long memberId = member.getId();

            redisService.publishNotificationToOtherServers(memberId, taskId, "", "이미지 생성에 실패했습니다.");
            redisService.removeFromQueue("image", taskId);
            taskRepository.save(task);
            memberRepository.save(member);
        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void handleCompleted(Long taskId, ImageWebhookEvent event, String runpodId) {
        try {
            ImageTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Image task not found"));

            task = task.updateStatus(Status.COMPLETED, runpodId);

            Long memberId = task.getCreator().getId();
            String prompt = event.getPrompt();

            List<String> images = event.getOutput() != null ? event.getOutput().getImages() : List.of();

            saveToDatabase(task.getId(), images);

            redisService.publishNotificationToOtherServers(memberId, taskId, prompt, images);

            redisService.removeFromQueue("image", taskId);
            taskRepository.save(task);
        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }


}