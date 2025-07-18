package com.ll.demo03.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.infrastructure.UGCJpaRepository;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.imageTask.controller.request.ImageWebhookEvent;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.notification.controller.response.NotificationResponse;
import com.ll.demo03.notification.service.NotificationService;

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
    private final UGCRepository ugcRepository;
    private final ObjectMapper objectMapper;
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

            UGC image = UGC.ofImage(imageUrl, imageTask);
            video.setIndex(0);
            UGCJpaRepository.save(video);

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

            Member member = imageTask.getCreator();
            member.setCredit(member.getCredit() + 1);
            String memberIdStr = String.valueOf(member.getId());


            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("imageUrl", new String[]{});
            payloadMap.put("prompt", imageTask.getPrompt());
            payloadMap.put("taskId", taskIdLong);

            imageTaskRepository.save(imageTask);

            redisTemplate.opsForList().remove("image:queue", 1,  String.valueOf(taskIdLong));

            try {
                NotificationResponse dto = NotificationResponse.builder()
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

            ImageTaskEntity imageTaskEntity = imageTaskJpaRepository.findById(taskIdLong)
                    .orElseThrow(() -> new EntityNotFoundException("Video task not found"));

            imageTaskEntity.setStatus("COMPLETED");

            Long memberId = imageTaskEntity.getMemberEntity().getId();
            String memberIdStr = String.valueOf(memberId);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("imageUrl", imageUrls);
            payloadMap.put("prompt", prompt);
            payloadMap.put("taskId", taskIdLong);

            imageTaskJpaRepository.save(imageTaskEntity);

            redisTemplate.opsForList().remove("image:queue", 1,  String.valueOf(taskIdLong));

            try {
                NotificationResponse dto = NotificationResponse.builder()
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