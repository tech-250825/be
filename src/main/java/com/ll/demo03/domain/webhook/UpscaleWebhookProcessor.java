package com.ll.demo03.domain.webhook;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import com.ll.demo03.domain.task.dto.ProgressResponse;
import com.ll.demo03.domain.task.entity.Task;
import com.ll.demo03.domain.task.repository.TaskRepository;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleImageUrlResponse;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleWebhookEvent;
import com.ll.demo03.domain.upscaledTask.entity.UpscaleTask;
import com.ll.demo03.domain.upscaledTask.repository.UpscaleTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpscaleWebhookProcessor {

    private final UpscaleTaskRepository upscaleTaskRepository;
    private final SseEmitterRepository sseEmitterRepository;
    private final ImageRepository imageRepository;
    private final TaskRepository taskRepository;

    public void processWebhookEvent(UpscaleWebhookEvent event) {
        String taskId = getTaskId(event);
        String originTaskId = event.getData().getInput().getOrigin_task_id();
        log.info("웹훅 이벤트 수신: {}", taskId);

        try {
            if (!isCompleted(event)) {
                log.info("Task not yet completed, status: {}", getStatus(event));
                notifyProcess(taskId, getStatus(event), getProgress(event));
                return;
            }

            Object resourceData = getResourceData(event);
            if (isResourceDataEmpty(resourceData)) {
                log.info("리소스 데이터가 아직 생성되지 않았습니다: {}", taskId);
                return;
            }

            saveToDatabase(taskId,originTaskId, resourceData);

            notifyClient(taskId, resourceData);

        } catch (Exception e) {
            log.error("웹훅 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public String getTaskId(UpscaleWebhookEvent event) {
        return event.getData().getTask_id();
    }


    public String getStatus(UpscaleWebhookEvent event) {
        return event.getData().getStatus();
    }

    public String getProgress(UpscaleWebhookEvent event) {
        Integer progress = event.getData().getOutput().getProgress();
        return String.valueOf(progress);
    }

    public Object getResourceData(UpscaleWebhookEvent event) {
        return event.getData().getOutput().getImage_url();
    }

    public boolean isCompleted(UpscaleWebhookEvent event) {
        return "completed".equals(event.getData().getStatus());
    }

    public boolean isResourceDataEmpty(Object resourceData) {
        String url = (String) resourceData;
        return url == null || url.isEmpty();
    }


    public void saveToDatabase(String taskId, String originTaskId, Object resourceData) {
        try {
            String imageUrl = (String) resourceData;

            UpscaleTask upscaleTask = upscaleTaskRepository.findByNewTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Upscale task not found"));

            Task task = taskRepository.findByTaskId(originTaskId)
                    .orElseThrow(() -> new EntityNotFoundException("Task not found"));

            Image image = Image.ofUpscale(imageUrl, task, upscaleTask);
            image.setImgIndex(0);
            imageRepository.save(image);

            log.info("✅ DB 저장 완료: taskId={}, imageUrl={}", taskId, imageUrl);
        } catch (Exception e) {
            log.error("DB 저장 중 오류 발생: {}", e.getMessage(), e);
            notifyClient(taskId, e.getMessage());
        }
    }

    public void notifyProcess(String taskId, String status, String progress) {
        try {

            Task task = taskRepository.findByTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Task not found"));

            Long memberId = task.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

            SseEmitter emitter = sseEmitterRepository.get(memberIdStr);

            if (emitter != null) {
                ProgressResponse response = new ProgressResponse(taskId, status, progress);

                try {
                    emitter.send(SseEmitter.event()
                            .name("result")
                            .data(response));

                } catch (Exception e) {
                    log.error("SSE 이벤트 전송 중 오류: {}", e.getMessage(), e);
                    sseEmitterRepository.removeTaskMapping(taskId);
                }
            } else {
                log.warn("❗ SSE 연결 없음: memberId={}, taskId={}", memberId, taskId);
            }
        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void notifyClient(String taskId, Object resourceData) {
        try {
            String imageUrl = (String) resourceData;

            UpscaleTask upscaleTask = upscaleTaskRepository.findByNewTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Upscale task not found"));

            Long memberId = upscaleTask.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

            SseEmitter emitter = sseEmitterRepository.get(memberIdStr);

            if (emitter != null) {
                UpscaleImageUrlResponse response = new UpscaleImageUrlResponse(imageUrl, taskId);

                try {
                    emitter.send(SseEmitter.event()
                            .name("result")
                            .data(response));

                    log.info("✅ 클라이언트에게 업스케일 이미지 URL 전송 완료: {}, memberId: {}", taskId, memberId);

                    emitter.complete();
                    sseEmitterRepository.removeTaskMapping(taskId);
                } catch (Exception e) {
                    log.error("SSE 이벤트 전송 중 오류: {}", e.getMessage(), e);
                    sseEmitterRepository.removeTaskMapping(taskId);
                }
            } else {
                log.warn("❗ 해당 사용자 ID에 대한 SSE 연결이 없습니다: {}, taskId: {}", memberId, taskId);
            }
        } catch (Exception e) {
            log.error("업스케일 SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}