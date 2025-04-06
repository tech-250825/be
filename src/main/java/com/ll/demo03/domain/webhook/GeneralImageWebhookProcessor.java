package com.ll.demo03.domain.webhook;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import com.ll.demo03.domain.task.dto.ImageUrlsResponse;
import com.ll.demo03.domain.task.dto.WebhookEvent;
import com.ll.demo03.domain.task.entity.Task;
import com.ll.demo03.domain.task.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@Slf4j
public class GeneralImageWebhookProcessor implements WebhookProcessor<WebhookEvent> {


    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SseEmitterRepository sseEmitterRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Override
    public void processWebhookEvent(WebhookEvent event) {
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
    public String getTaskId(WebhookEvent event) {
        return event.getData().getTask_id();
    }

    @Override
    public String getStatus(WebhookEvent event) {
        return event.getData().getStatus();
    }

    @Override
    public Object getResourceData(WebhookEvent event) {
        return event.getData().getOutput().getImage_urls();
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
            @SuppressWarnings("unchecked")
            List<String> imageUrls = (List<String>) resourceData;

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
            notifyClient(taskId, e.getMessage());
        }
    }

    @Override
    public void notifyClient(String taskId, Object resourceData) {
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

            if (emitter != null) {
                ImageUrlsResponse response = new ImageUrlsResponse(imageUrls, taskId);

                try {
                    emitter.send(SseEmitter.event()
                            .name("result")
                            .data(response));

                    log.info("✅ 클라이언트에게 이미지 URL 전송 완료: {}, memberId: {}", taskId, memberId);

                    sseEmitterRepository.removeTaskMapping(taskId);
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
}