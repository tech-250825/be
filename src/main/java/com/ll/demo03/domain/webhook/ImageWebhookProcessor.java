package com.ll.demo03.domain.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.imageTask.dto.ImageWebhookEvent;
import com.ll.demo03.domain.imageTask.entity.ImageTask;
import com.ll.demo03.domain.imageTask.repository.ImageTaskRepository;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.notification.dto.NotificationResponse;
import com.ll.demo03.domain.notification.entity.Notification;
import com.ll.demo03.domain.notification.entity.NotificationStatus;
import com.ll.demo03.domain.notification.entity.NotificationType;
import com.ll.demo03.domain.notification.repository.NotificationRepository;
import com.ll.demo03.domain.notification.service.NotificationService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageWebhookProcessor implements WebhookProcessor<ImageWebhookEvent> {

    private final ImageTaskRepository imageTaskRepository;
    private final ImageRepository imageRepository;
    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final StringRedisTemplate redisTemplate;

    public void processWebhookEvent(ImageWebhookEvent event) {
        log.info("event" , event);
        Long taskIdLong = getTaskId(event);

        String prompt = getPrompt(event);
        String imageUrl = getImageUrl(event);
        log.info("웹훅 이벤트 수신: {}", taskIdLong);

        try {
            String status = getStatus(event);
            if ("FAILED".equals(status)) {
                log.error("Task failed: {}", taskIdLong);
                notifyClient(taskIdLong);
                return;
            }
            if("COMPLETED".equals(status)){
                saveToDatabase(taskIdLong, imageUrl);
                notifyResult(taskIdLong, imageUrl, prompt);
                return;
            }

            Object resourceData = getResourceData(event);
            if (isResourceDataEmpty(resourceData)) {
                log.info("리소스 데이터가 아직 생성되지 않았습니다: {}", taskIdLong);
                log.error("Task failed: {}", taskIdLong);
                notifyClient(taskIdLong);
            }

        } catch (Exception e) {
            log.error("웹훅 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
            notifyClient(taskIdLong);
        }
    }

    private String getImageUrl(ImageWebhookEvent event) {
        return event.getOutput().getImages();
    }

    public Long getTaskId(ImageWebhookEvent event) {
        return event.getInput().getPayload().getTaskId();
    }

    public String getPrompt(ImageWebhookEvent event) {
        if (event.getInput() != null && event.getInput().getPayload() != null) {
            return event.getInput().getPayload().getPositivePrompt();
        }
        return null;
    }

    @Override
    public String getStatus(ImageWebhookEvent event) {
        return event.getStatus();
    }

    @Override
    public Object getResourceData(ImageWebhookEvent event) {
        return event.getOutput() != null ? event.getOutput().getImages() : null;
    }


    @Override
    public boolean isCompleted(ImageWebhookEvent event) {
        return "COMPLETED".equalsIgnoreCase(event.getStatus());
    }

    @Override
    public boolean isResourceDataEmpty(Object resourceData) {
        String url = (String) resourceData;
        return url == null || url.isEmpty();
    }

    public void saveToDatabase(Long taskIdLong, Object resourceData) {
        try {
            String imageUrl = (String) resourceData;

            ImageTask imageTask = imageTaskRepository.findById(taskIdLong)
                    .orElseThrow(() -> new EntityNotFoundException("Video task not found"));

            Image video = Image.of(imageUrl, imageTask);
            video.setImgIndex(0);
            imageRepository.save(video);

            log.info("✅ DB 저장 완료: taskId={}, videoUrl={}", taskIdLong, imageUrl);
        } catch (Exception e) {
            log.error("DB 저장 중 오류 발생: {}", e.getMessage(), e);
        }
    }


    public void notifyClient(Long taskIdLong) {
        try {
            ImageTask imageTask = imageTaskRepository.findById(taskIdLong)
                    .orElseThrow(() -> new EntityNotFoundException("Video task not found"));

            imageTask.setStatus("FAILED");

            Member member = imageTask.getMember();
            member.setCredit(member.getCredit() + 1);
            String memberIdStr = String.valueOf(member.getId());

            Notification notification = new Notification();
            notification.setMember(imageTask.getMember());
            notification.setType(NotificationType.IMAGE);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setMessage("영상 생성 실패");
            notification.setRead(false);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("imageUrl", new String[]{});
            payloadMap.put("prompt", imageTask.getPrompt());
            payloadMap.put("taskId", taskIdLong);

            notificationRepository.save(notification);
            imageTaskRepository.save(imageTask);

            redisTemplate.opsForList().remove("image:queue", 1,  String.valueOf(taskIdLong));

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

    public void notifyResult(Long taskIdLong, Object imageUrl, String prompt) {
        try {
            List<String> imageUrls;

            if (imageUrl instanceof String) {
                imageUrls = List.of((String) imageUrl);
            } else {
                @SuppressWarnings("unchecked")
                List<String> castedUrls = (List<String>) imageUrl;
                imageUrls = castedUrls;
            }

            ImageTask imageTask = imageTaskRepository.findById(taskIdLong)
                    .orElseThrow(() -> new EntityNotFoundException("Video task not found"));

            imageTask.setStatus("SUCCEDED");

            Long memberId = imageTask.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

            Notification notification = new Notification();
            notification.setMember(imageTask.getMember());
            notification.setType(NotificationType.IMAGE);
            notification.setStatus(NotificationStatus.SUCCESS);
            notification.setMessage("영상 생성 완료");
            notification.setRead(false);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("imageUrl", imageUrls);
            payloadMap.put("prompt", prompt);
            payloadMap.put("taskId", taskIdLong);

            notificationRepository.save(notification);
            imageTaskRepository.save(imageTask);

            redisTemplate.opsForList().remove("image:queue", 1,  String.valueOf(taskIdLong));

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

                String redisKey = "notification:image:" + memberIdStr;
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