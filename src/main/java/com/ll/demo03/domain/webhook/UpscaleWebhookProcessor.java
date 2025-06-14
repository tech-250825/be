package com.ll.demo03.domain.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.notification.entity.Notification;
import com.ll.demo03.domain.notification.entity.NotificationStatus;
import com.ll.demo03.domain.notification.entity.NotificationType;
import com.ll.demo03.domain.notification.repository.NotificationRepository;
import com.ll.demo03.domain.notification.service.NotificationService;
import com.ll.demo03.domain.task.entity.Task;
import com.ll.demo03.domain.task.repository.TaskRepository;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleWebhookEvent;
import com.ll.demo03.domain.upscaledTask.entity.UpscaleTask;
import com.ll.demo03.domain.upscaledTask.repository.UpscaleTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpscaleWebhookProcessor implements WebhookProcessor<UpscaleWebhookEvent> {

    private final UpscaleTaskRepository upscaleTaskRepository;
    private final TaskRepository taskRepository;
    private final ImageRepository imageRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void processWebhookEvent(UpscaleWebhookEvent event) {
        String taskId = getTaskId(event);

        String originTaskId = event.getData().getInput().getOrigin_task_id();
        log.info("업스케일 웹훅 이벤트 수신: {}", taskId);

        try {
            if (!isCompleted(event)) {
                String progress = getProgress(event);
                notifyProcess(taskId, progress);
                return;
            }

            Object resourceData = getResourceData(event);
            if (isResourceDataEmpty(resourceData)) {
                log.info("리소스 데이터가 아직 생성되지 않았습니다: {}", taskId);
                return;
            }

            saveToDatabase(taskId, originTaskId, resourceData);

            notifyResult(taskId, resourceData);

        } catch (Exception e) {
            notifyAlarm(taskId);
            log.error("업스케일 웹훅 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getTaskId(UpscaleWebhookEvent event) {
        return event.getData().getTask_id();
    }

    @Override
    public String getStatus(UpscaleWebhookEvent event) {
        return event.getData().getStatus();
    }

    public String getProgress(UpscaleWebhookEvent event) {
        Integer progress = event.getData().getOutput().getProgress();
        return String.valueOf(progress);
    }

    @Override
    public Object getResourceData(UpscaleWebhookEvent event) {
        return event.getData().getOutput().getImage_url();
    }

    @Override
    public boolean isCompleted(UpscaleWebhookEvent event) {
        return "completed".equals(event.getData().getStatus());
    }

    @Override
    public boolean isResourceDataEmpty(Object resourceData) {
        String url = (String) resourceData;
        return url == null || url.isEmpty();
    }

    @Override
    public void saveToDatabase(String taskId, Object resourceData) {
        // This method signature is required by interface but not used
        // Use the overloaded version below
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

            log.info("✅ DB에 업스케일 이미지 저장 완료: taskId={}, imageUrl={}", taskId, imageUrl);
        } catch (Exception e) {
            log.error("DB 저장 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void notifyProcess(String taskId, String progress) {
        try {
            UpscaleTask upscaleTask = upscaleTaskRepository.findByNewTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Upscale task not found"));

            Long memberId = upscaleTask.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

            Notification notification = new Notification();
            notification.setType(NotificationType.UPSCALE);
            notification.setMessage("이미지 업스케일 중입니다.");
            notification.setStatus(NotificationStatus.PENDING);
            notification.setRead(false);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("requestId", upscaleTask.getId());
            payloadMap.put("imageUrl", new String[]{});
            payloadMap.put("taskId", taskId);
            payloadMap.put("progress", progress);
            payloadMap.put("type", "upscale");


            try {
                String payloadJson = objectMapper.writeValueAsString(payloadMap);
                notification.setPayload(payloadJson);
                notificationRepository.save(notification);

                String redisKey = "notification:upscale:" + memberIdStr;
                String notificationJson = objectMapper.writeValueAsString(notification);
                redisTemplate.opsForValue().set(redisKey, notificationJson);

                notificationService.publishNotificationToOtherServers(memberIdStr, notificationJson);
            } catch (JsonProcessingException e) {
                log.error("payload 직렬화 실패", e);
            }

        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void notifyResult(String taskId, Object resourceData) {
        try {
            String imageUrl = (String) resourceData;

            UpscaleTask upscaleTask = upscaleTaskRepository.findByNewTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Upscale task not found"));

            Long memberId = upscaleTask.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

            Notification notification = new Notification();
            notification.setType(NotificationType.UPSCALE);
            notification.setStatus(NotificationStatus.SUCCESS);
            notification.setMessage("이미지 업스케일 완료");
            notification.setRead(false);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("requestId", upscaleTask.getId());
            payloadMap.put("imageUrl", imageUrl);
            payloadMap.put("taskId", taskId);
            payloadMap.put("type", "upscale");

            redisTemplate.opsForList().remove("upscale:queue", 1, taskId);


            try {
                String payloadJson = objectMapper.writeValueAsString(payloadMap);
                notification.setPayload(payloadJson);
                notificationRepository.save(notification);

                String redisKey = "notification:upscale:" + memberIdStr;
                String notificationJson = objectMapper.writeValueAsString(notification);
                redisTemplate.opsForValue().set(redisKey, notificationJson);

                notificationService.publishNotificationToOtherServers(memberIdStr, notificationJson);
            } catch (JsonProcessingException e) {
                log.error("payload 직렬화 실패", e);
            }

        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void notifyAlarm(String taskId) {
        try {
            UpscaleTask upscaleTask = upscaleTaskRepository.findByNewTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Upscale task not found"));

            Long memberId = upscaleTask.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

            Notification notification = new Notification();
            notification.setType(NotificationType.UPSCALE);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setMessage("이미지 업스케일 실패");
            notification.setRead(false);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("requestId", upscaleTask.getId());
            payloadMap.put("imageUrl", new String[]{});
            payloadMap.put("taskId", taskId);
            payloadMap.put("type", "upscale");

            redisTemplate.opsForList().remove("upscale:queue", 1, taskId);

            try {
                String payloadJson = objectMapper.writeValueAsString(payloadMap);
                notification.setPayload(payloadJson);
                notificationRepository.save(notification);

                String redisKey = "notification:upscale:" + memberIdStr;
                String notificationJson = objectMapper.writeValueAsString(notification);
                redisTemplate.opsForValue().set(redisKey, notificationJson);

                notificationService.publishNotificationToOtherServers(memberIdStr, notificationJson);
            } catch (JsonProcessingException e) {
                log.error("payload 직렬화 실패", e);
            }

        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

}