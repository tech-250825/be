package com.ll.demo03.domain.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.notification.dto.NotificationMapper;
import com.ll.demo03.domain.notification.dto.NotificationResponse;
import com.ll.demo03.domain.notification.entity.Notification;
import com.ll.demo03.domain.notification.entity.NotificationStatus;
import com.ll.demo03.domain.notification.entity.NotificationType;
import com.ll.demo03.domain.notification.repository.NotificationRepository;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import com.ll.demo03.domain.task.dto.WebhookEvent;
import com.ll.demo03.domain.task.entity.Task;
import com.ll.demo03.domain.task.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.net.*;


import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeneralImageWebhookProcessor implements WebhookProcessor<WebhookEvent> {

    private final TaskRepository taskRepository;
    private final SseEmitterRepository sseEmitterRepository;
    private final ImageRepository imageRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;
    private final S3Client s3Client;

    @Value("${r2.bucket}")
    private String bucket;

    @Override
    public void processWebhookEvent(WebhookEvent event) {
        String taskId = getTaskId(event);
        log.info("웹훅 이벤트 수신: {}", taskId);
        String prompt = getPrompt(event);
        String ratio = getRatio(event);

        try {
            if (!isCompleted(event)) {
                String progress=getProgress(event);
                notifyProcess(taskId, progress, ratio);
                return;
            }

            Object resourceData = getResourceData(event);
            if (isResourceDataEmpty(resourceData)) {
                log.info("리소스 데이터가 아직 생성되지 않았습니다: {}", taskId);
                return;
            }

            saveToDatabase(taskId, resourceData);

            notifyResult(taskId, resourceData, ratio);

        } catch (Exception e) {
            notifyAlarm(taskId, ratio);
            log.error("웹훅 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getTaskId(WebhookEvent event) {
        return event.getData().getTask_id();
    }

    @Override
    public String getStatus(WebhookEvent event) {
        return event.getData().getStatus();
    }

    public String getProgress(WebhookEvent event) {
        String result = event.getData().getOutput().getProgress().toString();
        return result;
    }

    @Override
    public Object getResourceData(WebhookEvent event) {
        return event.getData().getOutput().getImage_urls();
    }

    public String getPrompt(WebhookEvent event) {
        return event.getData().getInput().getPrompt();
    }

    public String getRatio(WebhookEvent event) {
        return event.getData().getInput().getAspect_ratio();
    }

    @Override
    public boolean isCompleted(WebhookEvent event) {
        return "completed".equals(event.getData().getStatus());
    }

    @Override
    public boolean isResourceDataEmpty(Object resourceData) {
        @SuppressWarnings("unchecked")
        List<String> imageUrls = (List<String>) resourceData;
        return imageUrls == null || imageUrls.isEmpty();
    }

    @Override
    public void saveToDatabase(String taskId, Object resourceData) {
        try {
            List<String> imageUrls = (List<String>) resourceData;
            imageUrls = downloadAndUploadToBucket(imageUrls);

            Task task = taskRepository.findByTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Task not found"));

            if (imageUrls != null && imageUrls.size() >= 4) {
                List<String> firstFourUrls = imageUrls.subList(0, 4);
                for (int i = 0; i < firstFourUrls.size(); i++) {
                    Image image = Image.of(firstFourUrls.get(i), task);
                    image.setImgIndex(i + 1);
                    imageRepository.save(image);
                }

                log.info("✅ DB에 다중 이미지 저장 완료: taskId={}, imageCount={}", taskId, firstFourUrls.size());
            }
        } catch (Exception e) {
            log.error("DB 저장 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void notifyProcess(String taskId, String progress,String ratio) {
        try {

            Task task = taskRepository.findByTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Task not found"));

            Long memberId = task.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

            SseEmitter emitter = sseEmitterRepository.get(memberIdStr);

                Notification notification = new Notification();
                notification.setType(NotificationType.IMAGE); // 예시
                notification.setMessage("이미지 생성 중입니다.");
                notification.setStatus(NotificationStatus.PENDING);
                notification.setRead(false);

                Map<String, Object> payloadMap = new HashMap<>();
                payloadMap.put("requestId", task.getId());
                payloadMap.put("imageUrl", new String[]{});
                payloadMap.put("prompt", task.getRawPrompt());
                payloadMap.put("ratio", ratio);
                payloadMap.put("taskId", taskId);
                payloadMap.put("progress", progress);

                try {
                    String payloadJson = objectMapper.writeValueAsString(payloadMap);
                    notification.setPayload(payloadJson);
                    notificationRepository.save(notification);
                } catch (JsonProcessingException e) {
                    log.error("payload 직렬화 실패", e);
                }
                if (emitter != null) {

                try {
                    NotificationResponse response = NotificationMapper.toResponse(notification);
                    emitter.send(SseEmitter.event()
                            .data(response));

                } catch (Exception e) {
                    log.error("SSE 이벤트 전송 중 오류: {}", e.getMessage(), e);
                    sseEmitterRepository.removeTaskMapping(memberIdStr);
                }
            } else {
                log.warn("❗ SSE 연결 없음: memberId={}, taskId={}", memberId, taskId);
            }
        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void notifyResult(String taskId, Object resourceData,String ratio) {
        try {
            List<String> imageUrls;

            if (resourceData instanceof String) {
                imageUrls = List.of((String) resourceData);
            } else {
                @SuppressWarnings("unchecked")
                List<String> castedUrls = (List<String>) resourceData;
                imageUrls = castedUrls;
            }

            Task task = taskRepository.findByTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Task not found"));

            Long memberId = task.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

            SseEmitter emitter = sseEmitterRepository.get(memberIdStr);

                Notification notification = new Notification();
                notification.setType(NotificationType.IMAGE); // 예시
                notification.setStatus(NotificationStatus.SUCCESS);
                notification.setMessage("이미지 생성 완료");
                notification.setRead(false);

                Map<String, Object> payloadMap = new HashMap<>();
                payloadMap.put("requestId", task.getId());
                payloadMap.put("imageUrl", imageUrls);
                payloadMap.put("prompt", task.getRawPrompt());
                payloadMap.put("ratio", ratio);
                payloadMap.put("taskId", taskId);

                try {
                    String payloadJson = objectMapper.writeValueAsString(payloadMap);
                    notification.setPayload(payloadJson);
                    notificationRepository.save(notification);
                } catch (JsonProcessingException e) {
                    log.error("payload 직렬화 실패", e);
                }
            if (emitter != null) {
                try {
                    NotificationResponse response = NotificationMapper.toResponse(notification);
                    emitter.send(SseEmitter.event()
                            .data(response));

                    log.info("✅ 클라이언트에게 이미지 URL 전송 완료: {}, memberId: {}", taskId, memberId);

                    sseEmitterRepository.removeTaskMapping(memberIdStr);
                } catch (Exception e) {
                    log.error("SSE 이벤트 전송 중 오류: {}", e.getMessage(), e);
                    sseEmitterRepository.removeTaskMapping(memberIdStr);
                }
            } else {
                log.warn("❗ SSE 연결 없음: memberId={}, taskId={}", memberId, taskId);
            }
        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void notifyAlarm(String taskId, String ratio) {
        try {
            Task task = taskRepository.findByTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Task not found"));

            Long memberId = task.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

            SseEmitter emitter = sseEmitterRepository.get(memberIdStr);

                Notification notification = new Notification();
                notification.setType(NotificationType.IMAGE); // 예시
                notification.setStatus(NotificationStatus.FAILED);
                notification.setMessage("이미지 생성 실패");
                notification.setRead(false);

                Map<String, Object> payloadMap = new HashMap<>();
                payloadMap.put("requestId", task.getId());
                payloadMap.put("imageUrl", new String[]{});
                payloadMap.put("prompt", task.getRawPrompt());
                payloadMap.put("ratio", ratio);
                payloadMap.put("taskId", taskId);


                try {
                    String payloadJson = objectMapper.writeValueAsString(payloadMap);
                    notification.setPayload(payloadJson);
                    notificationRepository.save(notification);
                } catch (JsonProcessingException e) {
                    log.error("payload 직렬화 실패", e);
                }

            if (emitter != null) {
                try {
                    NotificationResponse response = NotificationMapper.toResponse(notification);
                    emitter.send(SseEmitter.event()
                            .data(response));

                    log.info("✅ 클라이언트에게 에러 알람 전송 실패 : {}, memberId: {}", taskId, memberId);

                    sseEmitterRepository.removeTaskMapping(memberIdStr);
                } catch (Exception e) {
                    log.error("SSE 이벤트 전송 중 오류: {}", e.getMessage(), e);
                    sseEmitterRepository.removeTaskMapping(memberIdStr);
                }
            } else {
                log.warn("❗ SSE 연결 없음: memberId={}, taskId={}", memberId, taskId);
            }
        } catch (Exception e) {
            log.error("SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public List<String> downloadAndUploadToBucket(List<String> midjourneyUrls) {
        List<String> bucketUrls = new ArrayList<>();
        for (String url : midjourneyUrls) {
            try {
                byte[] imageBytes = downloadImage(url);

                String fileName = UUID.randomUUID() + ".png";
                String objectKey = "generated/" + fileName;

                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .contentType("image/png")
                        .build();

                s3Client.putObject(request, RequestBody.fromBytes(imageBytes));

                String bucketUrl = "https://image.hoit.my/" + objectKey;
                bucketUrls.add(bucketUrl);

            } catch (Exception e) {
                log.error("이미지 다운로드 또는 업로드 실패: {}", url, e);
            }
        }
        return bucketUrls;
    }
    private byte[] downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("5.161.103.41", 88));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);

        // 2. 헤더 설정
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        connection.setRequestProperty("Referer", "https://www.midjourney.com/");
        connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");

        connection.setInstanceFollowRedirects(true);
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("이미지 다운로드 실패, 응답 코드: " + responseCode);
        }

        try (InputStream in = connection.getInputStream()) {
            return in.readAllBytes();
        } finally {
            connection.disconnect();
        }
    }

}