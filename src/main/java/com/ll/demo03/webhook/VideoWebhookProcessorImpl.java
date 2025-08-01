package com.ll.demo03.webhook;

import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.global.controller.request.WebhookEvent;
import com.ll.demo03.global.domain.Status;
import com.ll.demo03.global.port.RedisService;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.domain.VideoTask;
import com.ll.demo03.videoTask.service.port.VideoTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class VideoWebhookProcessorImpl implements WebhookProcessor<WebhookEvent> {

    private final VideoTaskRepository taskRepository;
    private final UGCRepository UGCRepository;
    private final RedisService redisService;

    public void processWebhookEvent(WebhookEvent event) {
        Long taskId = event.getTaskId();
        String status = event.getStatus();
        String runpodId = event.getRunpodId();

        try {
            switch (status){
                case "FAILED" -> handleFailed(taskId, runpodId);
                case "COMPLETED" -> handleCompleted(taskId, event, runpodId);
                default -> handleFailed(taskId, runpodId);
            }
        } catch (Exception e) {
            log.error("웹훅 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
            handleFailed(taskId, runpodId);
        }
    }

    public void saveToDatabase(Long taskId, String url) {
        try {

            VideoTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Video task not found"));

            UGC ugc = UGC.ofVideo(url, task);
            UGCRepository.save(ugc);

            log.info("✅ DB 저장 완료: taskId={}, videoUrl={}", taskId, ugc);
        } catch (Exception e) {
            log.error("DB 저장 중 오류 발생: {}", e.getMessage(), e);
        }
    }


    public void handleFailed(Long taskId, String runpodId) {
        try {
            VideoTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Video task not found"));

            task = task.updateStatus(Status.FAILED, runpodId);

            Member member = task.getCreator();
            member.increaseCredit( task.getResolutionProfile().getBaseCreditCost() * (int) Math.ceil(task.getNumFrames() / 40.0));

            redisService.publishNotificationToOtherServers(member.getId(), taskId, "이미지 생성에 실패했습니다", "");
            redisService.removeFromQueue("video", taskId);
            taskRepository.save(task);
        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void handleCompleted(Long taskId, WebhookEvent event, String runpodId) {
        try {
            VideoTask task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Video task not found"));

            task = task.updateStatus(Status.COMPLETED, runpodId); //task.updateStatus는 POJO를 반환하기 때문에 JPA에서 더티 체킹 안됨. 상태를 바꾸는 대신 새로운 객체를 반환하는 방식이다.  꼭 반환 후 저장 필요!

            Long memberId = task.getCreator().getId(); //이거 셋 dto화 하는게 나을듯 (결과 올 떄 파싱 용도)
            String url = event.getImages();
            String prompt = event.getPrompt();

            saveToDatabase(task.getId(), url);

            redisService.publishNotificationToOtherServers(memberId, taskId, prompt, url); //redis에 전송 실패하더라도 db에는 적재될 수 있게 !
            redisService.removeFromQueue("video", taskId);
            taskRepository.save(task);
        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

}
