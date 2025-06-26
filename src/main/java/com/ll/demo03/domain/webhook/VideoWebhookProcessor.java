package com.ll.demo03.domain.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.notification.dto.NotificationResponse;
import com.ll.demo03.domain.notification.entity.Notification;
import com.ll.demo03.domain.notification.entity.NotificationStatus;
import com.ll.demo03.domain.notification.entity.NotificationType;
import com.ll.demo03.domain.notification.repository.NotificationRepository;
import com.ll.demo03.domain.notification.service.NotificationService;
import com.ll.demo03.domain.task.entity.Task;
import com.ll.demo03.domain.task.repository.TaskRepository;
import com.ll.demo03.domain.videoTask.dto.VideoWebhookEvent;
import com.ll.demo03.domain.videoTask.entity.VideoTask;
import com.ll.demo03.domain.videoTask.repository.VideoTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoWebhookProcessor implements WebhookProcessor<VideoWebhookEvent> {

    private final VideoTaskRepository videoTaskRepository;
    private final ImageRepository imageRepository;
    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final StringRedisTemplate redisTemplate;

    public void processWebhookEvent(VideoWebhookEvent event) {
        String taskId = getTaskId(event);

        String prompt = getPrompt(event);
        log.info("웹훅 이벤트 수신: {}", taskId);

        try {
            String status = getStatus(event);
            if ("pending".equals(status) || "processing".equals(status)) {
                String process = getProcess(event);
                notifyProcess(taskId, String.valueOf(process));
                return;
            } else if ("failed".equals(status)) {
                log.error("Task failed: {}", taskId);
                notifyClient(taskId);
                return;
            }
            Object resourceData = getResourceData(event);
            if (isResourceDataEmpty(resourceData)) {
                log.info("리소스 데이터가 아직 생성되지 않았습니다: {}", taskId);
                return;
            }

            saveToDatabase(taskId, resourceData);

            notifyResult(taskId, resourceData, prompt);

        } catch (Exception e) {
            log.error("웹훅 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
            notifyClient(taskId);
        }
    }

    @Override
    public String getTaskId(VideoWebhookEvent event) {
        return event.getData().getTaskId();
    }

    public String getProcess(VideoWebhookEvent event){
        return event.getData().getStatus();
    }

    public String getPrompt(VideoWebhookEvent event) {
        return event.getData().getInput().getPrompt();
    }

    @Override
    public String getStatus(VideoWebhookEvent event) {
        return event.getData().getStatus();
    }

    @Override
    public Object getResourceData(VideoWebhookEvent event) {
        return event.getData().getOutput().getVideoUrl();
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

            Image video = Image.ofVideo(videoUrl, videoTask);
            video.setImgIndex(0);
            imageRepository.save(video);

            log.info("✅ DB 저장 완료: taskId={}, videoUrl={}", taskId, videoUrl);
        } catch (Exception e) {
            log.error("DB 저장 중 오류 발생: {}", e.getMessage(), e);
        }
    }


    public void notifyClient(String taskId) {
        try {
            VideoTask videoTask = videoTaskRepository.findByTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Video task not found"));

            Long memberId = videoTask.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

                Notification notification = new Notification();
                notification.setMember(videoTask.getMember());
                notification.setType(NotificationType.VIDEO);
                notification.setStatus(NotificationStatus.FAILED);
                notification.setMessage("영상 생성 실패");
                notification.setRead(false);

                Map<String, Object> payloadMap = new HashMap<>();
                payloadMap.put("requestId", videoTask.getId());
                payloadMap.put("imageUrl", new String[]{});
                payloadMap.put("prompt", videoTask.getPrompt());
                payloadMap.put("taskId", taskId);

            notificationRepository.save(notification);

            redisTemplate.opsForList().remove("video:queue", 1, taskId);

                try {
                    String payloadJson = objectMapper.writeValueAsString(payloadMap);
                    notification.setPayload(payloadJson);

                    NotificationResponse dto = NotificationResponse.builder()
                            .id(notification.getId())
                            .type(notification.getType())
                            .status(notification.getStatus())
                            .message(notification.getMessage())
                            .isRead(notification.isRead())
                            .createdAt(notification.getCreatedAt())
                            .modifiedAt(notification.getModifiedAt())
                            .payload(payloadMap)
                            .build();

                    String redisKey = "notification:video:" + memberIdStr;
                    String notificationJson = objectMapper.writeValueAsString(dto);
                    redisTemplate.opsForValue().set(redisKey, notificationJson);

                    notificationService.publishNotificationToOtherServers(memberIdStr, notificationJson);
                } catch (JsonProcessingException e) {
                    log.error("payload 직렬화 실패", e);
                }

        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void notifyProcess(String taskId, String progress) {
        try {

            VideoTask videoTask = videoTaskRepository.findByTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Video task not found"));

            Long memberId = videoTask.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

                Notification notification = new Notification();
                notification.setMember(videoTask.getMember());
                notification.setType(NotificationType.VIDEO);
                notification.setMessage("영상 생성 중입니다.");
                notification.setStatus(NotificationStatus.PENDING);
                notification.setRead(false);

                Map<String, Object> payloadMap = new HashMap<>();
                payloadMap.put("requestId", videoTask.getId());
                payloadMap.put("imageUrl", new String[]{});
                payloadMap.put("prompt", videoTask.getPrompt());
                payloadMap.put("taskId", taskId);
                payloadMap.put("progress", progress);


                try {
                    String payloadJson = objectMapper.writeValueAsString(payloadMap);
                    notification.setPayload(payloadJson);

                    LocalDateTime now = LocalDateTime.now();

                    NotificationResponse dto = NotificationResponse.builder()
                            .id(1L)
                            .type(notification.getType())
                            .status(notification.getStatus())
                            .message(notification.getMessage())
                            .isRead(notification.isRead())
                            .createdAt(now)
                            .modifiedAt(now)
                            .payload(payloadMap)
                            .build();

                    String redisKey = "notification:video:" + memberIdStr;

                    String notificationJson = objectMapper.writeValueAsString(dto);
                    redisTemplate.opsForValue().set(redisKey, notificationJson);

                    notificationService.publishNotificationToOtherServers(memberIdStr, notificationJson);
                } catch (JsonProcessingException e) {
                    log.error("payload 직렬화 실패", e);
                }
        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void notifyResult(String taskId, Object resourceData, String prompt) {
        try {
            List<String> imageUrls;

            if (resourceData instanceof String) {
                imageUrls = List.of((String) resourceData);
            } else {
                @SuppressWarnings("unchecked")
                List<String> castedUrls = (List<String>) resourceData;
                imageUrls = castedUrls;
            }

            VideoTask videoTask = videoTaskRepository.findByTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Video task not found"));

            Long memberId = videoTask.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

                Notification notification = new Notification();
                notification.setMember(videoTask.getMember());
                notification.setType(NotificationType.VIDEO);
                notification.setStatus(NotificationStatus.SUCCESS);
                notification.setMessage("영상 생성 완료");
                notification.setRead(false);

            Map<String, Object> payloadMap = new HashMap<>();
                payloadMap.put("requestId", videoTask.getId());
                payloadMap.put("imageUrl", imageUrls);
                payloadMap.put("prompt", prompt);
                payloadMap.put("taskId", taskId);

            notificationRepository.save(notification);
            redisTemplate.opsForList().remove("video:queue", 1, taskId);

                try {
                    String payloadJson = objectMapper.writeValueAsString(payloadMap);
                    notification.setPayload(payloadJson);

                    NotificationResponse dto = NotificationResponse.builder()
                            .id(notification.getId())
                            .type(notification.getType())
                            .status(notification.getStatus())
                            .message(notification.getMessage())
                            .isRead(notification.isRead())
                            .createdAt(notification.getCreatedAt())
                            .modifiedAt(notification.getModifiedAt())
                            .payload(payloadMap)
                            .build();

                    String redisKey = "notification:video:" + memberIdStr;
                    String notificationJson = objectMapper.writeValueAsString(dto);
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
