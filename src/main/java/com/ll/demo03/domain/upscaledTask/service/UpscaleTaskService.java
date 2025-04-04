package com.ll.demo03.domain.upscaledTask.service;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import com.ll.demo03.domain.task.dto.AckInfo;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleImageUrlResponse;
import com.ll.demo03.domain.upscaledTask.dto.UpscaleWebhookEvent;
import com.ll.demo03.domain.upscaledTask.entity.UpscaleTask;
import com.ll.demo03.domain.upscaledTask.repository.UpscaleTaskRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

import com.mashape.unirest.http.HttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UpscaleTaskService {

    @Value("${piapi.api.key}")
    private String piApiKey;

    @Value("${r2.bucket}")
    private String bucket;

    private final Map<String, AckInfo> pendingAcks = new ConcurrentHashMap<>();

    private final UpscaleTaskRepository upscaleTaskRepository;
    private final ImageRepository imageRepository;
    private final SseEmitterRepository sseEmitterRepository;

    public String createUpscaleImage(String taskId, String imageIndex,  String webhook) {
        try {

            Unirest.setTimeouts(0, 0);
            Unirest.setTimeouts(0, 0);
            HttpResponse<String> response = (HttpResponse<String>) Unirest.post("https://api.piapi.ai/api/v1/task")
                    .header("x-api-key", piApiKey)
                    .header("Content-Type", "application/json")
                    .body("{\n    \"model\": \"midjourney\",\n    \"task_type\": \"upscale\",\n    \"input\": {\n        \"origin_task_id\": \""+ taskId+ "\",\n        \"index\": \""+imageIndex+"\"\n    },\n    \"config\": {\n        \"service_mode\": \"\",\n        \"webhook_config\": {\n            \"endpoint\": \"" + webhook + "\",\n            \"secret\": \"123456\"\n        }\n    }\n}")
                    .asString();

            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void processWebhookEvent(UpscaleWebhookEvent event) {
        log.info("업스케일 웹훅 이벤트 수신: {}", event.getData().getTask_id());

        try {
            if (!"completed".equals(event.getData().getStatus())) {
                log.info("Task not yet completed, status: {}", event.getData().getStatus());
                return;
            }

            String taskId = event.getData().getTask_id();
            String imageUrl = event.getData().getOutput().getImage_url();

            if (imageUrl == null || imageUrl.isEmpty()) {
                log.info("업스케일된 이미지 URL이 아직 생성되지 않았습니다: {}", taskId);
                return;
            }

            UpscaleTask upscaleTask = upscaleTaskRepository.findByNewTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Upscale task not found"));


            Image image = Image.ofUpscale(imageUrl, upscaleTask);
            image.setImgIndex(0);
            imageRepository.save(image);

            Long memberId = upscaleTask.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

            SseEmitter emitter = sseEmitterRepository.get(memberIdStr);

            if (emitter != null) {
                UpscaleImageUrlResponse response = new UpscaleImageUrlResponse(imageUrl, taskId);

                try {
                    emitter.send(SseEmitter.event()
                            .name("result")
                            .data(response));

                    log.info("클라이언트에게 업스케일 이미지 URL 전송 완료: {}, memberId: {}", taskId, memberId);


                    emitter.complete();
                    sseEmitterRepository.removeTaskMapping(taskId);
                } catch (Exception e) {
                    log.error("SSE 이벤트 전송 중 오류: {}", e.getMessage(), e);
                    sseEmitterRepository.remove(memberIdStr);
                }
            } else {
                log.warn("해당 사용자 ID에 대한 SSE 연결이 없습니다: {}, taskId: {}", memberId, taskId);
            }
        } catch (Exception e) {
            log.error("업스케일 웹훅 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
