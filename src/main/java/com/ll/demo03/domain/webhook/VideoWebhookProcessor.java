package com.ll.demo03.domain.webhook;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleImageUrlResponse;
import com.ll.demo03.domain.videoTask.dto.VideoWebhookEvent;
import com.ll.demo03.domain.videoTask.entity.VideoTask;
import com.ll.demo03.domain.videoTask.repository.VideoTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j
public class VideoWebhookProcessor implements WebhookProcessor<VideoWebhookEvent> {

    @Autowired
    private VideoTaskRepository videoTaskRepository;

    @Autowired
    private SseEmitterRepository sseEmitterRepository;

    @Autowired
    private ImageRepository imageRepository;

    public void processWebhookEvent(VideoWebhookEvent event) {
        String taskId = getTaskId(event);
        log.info("웹훅 이벤트 수신: {}", taskId);

        try {
            if (!isCompleted(event)) {
                log.info("Task not yet completed, status: {}", getStatus(event));
                notifyClient(taskId, getStatus(event));
                return;
            }

            Object resourceData = getResourceData(event);
            if (isResourceDataEmpty(resourceData)) {
                log.info("리소스 데이터가 아직 생성되지 않았습니다: {}", taskId);
                return;
            }

            saveToDatabase(taskId, resourceData);

            notifyClient(taskId, resourceData);

        } catch (Exception e) {
            log.error("웹훅 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getTaskId(VideoWebhookEvent event) {
        return event.getData().getTaskId();
    }

    @Override
    public String getStatus(VideoWebhookEvent event) {
        return event.getData().getStatus();
    }

    @Override
    public Object getResourceData(VideoWebhookEvent event) {
        return event.getData().getOutput().getDownloadUrl();
    }

    @Override
    public boolean isCompleted(VideoWebhookEvent event) {
        return "completed".equals(event.getData().getStatus());
    }

    @Override
    public boolean isResourceDataEmpty(Object resourceData) {
        String url = (String) resourceData;
        return url == null || url.isEmpty();
    }

    @Override
    public void saveToDatabase(String taskId, Object resourceData) {
        try {
            String videoUrl = (String) resourceData;

            VideoTask videoTask = videoTaskRepository.findByTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Video task not found"));

            Image image = Image.ofVideo(videoUrl, videoTask);
            image.setImgIndex(0);
            imageRepository.save(image);

            log.info("✅ DB 저장 완료: taskId={}, videoUrl={}", taskId, videoUrl);
        } catch (Exception e) {
            log.error("DB 저장 중 오류 발생: {}", e.getMessage(), e);
            notifyClient(taskId, e.getMessage());
        }
    }

    @Override
    public void notifyClient(String taskId, Object resourceData) {
        try {
            String videoUrl = (String) resourceData;

            VideoTask videoTask = videoTaskRepository.findByTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Video task not found"));

            Long memberId = videoTask.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

            SseEmitter emitter = sseEmitterRepository.get(memberIdStr);

            if (emitter != null) {
                UpscaleImageUrlResponse response = new UpscaleImageUrlResponse(videoUrl, taskId);

                try {
                    emitter.send(SseEmitter.event().name("result").data(response));
                    log.info("✅ 클라이언트 SSE 전송 완료: {}, memberId: {}", taskId, memberId);

                    emitter.complete();
                    sseEmitterRepository.removeTaskMapping(taskId);
                } catch (Exception e) {
                    log.error("SSE 이벤트 전송 중 오류: {}", e.getMessage(), e);
                }
            } else {
                log.warn("❗ SSE 연결 없음: memberId={}, taskId={}", memberId, taskId);
            }
        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
