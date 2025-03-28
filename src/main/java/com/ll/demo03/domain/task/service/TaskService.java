package com.ll.demo03.domain.task.service;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.referenceImage.service.ReferenceImageService;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import com.ll.demo03.domain.task.dto.ImageUrlsResponse;
import com.ll.demo03.domain.task.dto.WebhookEvent;
import com.ll.demo03.domain.task.entity.Task;
import com.ll.demo03.domain.task.repository.TaskRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mashape.unirest.http.HttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TaskService {

    @Value("${piapi.api.key}")
    private String piApiKey;

    @Value("${r2.bucket}")
    private String bucket;

    private final RestTemplate restTemplate;
    private final S3Client s3Client;
    private final ReferenceImageService referenceImageService;
    private final TaskRepository taskRepository;
    private final ImageRepository imageRepository;
    private final SseEmitterRepository sseEmitterRepository;

    public String createImage(String prompt, String ratio, String cref, String sref,  String webhook) {
        try {
            System.out.println("prompt: " + prompt);
            if(sref !=null ){
                prompt = prompt + " --sref " + sref;
            }
            if(cref !=null ){
                prompt = prompt + " --cref " + cref;
            }
            Unirest.setTimeouts(0, 0);
            HttpResponse<String> response = (HttpResponse<String>) Unirest.post("https://api.piapi.ai/api/v1/task")
                    .header("x-api-key", piApiKey)
                    .header("Content-Type", "application/json")
                    .body("{\r\n    \"model\": \"midjourney\",\r\n    \"task_type\": \"imagine\",\r\n    \"input\": {\r\n        \"prompt\": \"" + prompt + "\",\r\n        \"aspect_ratio\": \"" + ratio + "\",\r\n        \"process_mode\": \"fast\",\r\n        \"skip_prompt_check\": false,\r\n        \"bot_id\": 0\r\n    },\r\n    \"config\": {\r\n        \"service_mode\": \"\",\r\n        \"webhook_config\": {\r\n            \"endpoint\": \"" + webhook+ "\",\r\n            \"secret\": \"123456\"\r\n        }\r\n    }\r\n}")
                    .asString();

            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public void processWebhookEvent(WebhookEvent event) {
        log.info("웹훅 이벤트 수신: {}", event.getData().getTask_id());

        try {
            if (!"completed".equals(event.getData().getStatus())) {
                log.info("Task not yet completed, status: {}", event.getData().getStatus());
                return;
            }

            String taskId = event.getData().getTask_id();

            List<String> imageUrls = event.getData().getOutput().getImage_urls();

            if (imageUrls == null || imageUrls.isEmpty()) {
                log.info("이미지 URL이 아직 생성되지 않았습니다: {}", taskId);
                return;
            }

            Task task = taskRepository.findByTaskId(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Task not found"));

            if (imageUrls != null && imageUrls.size() >= 4) {
                List<String> firstFourUrls = imageUrls.subList(0, 4);
                for (int i = 0; i < firstFourUrls.size(); i++) {
                    Image image = Image.of(firstFourUrls.get(i), task);
                    image.setImgIndex(i + 1);
                    imageRepository.save(image);
                }
            }

            Long memberId = task.getMember().getId();
            String memberIdStr = String.valueOf(memberId);

            SseEmitter emitter = sseEmitterRepository.get(memberIdStr);

            if (emitter != null) {
                ImageUrlsResponse response = new ImageUrlsResponse(imageUrls, taskId);

                try {
                    emitter.send(SseEmitter.event()
                            .name("result")
                            .data(response));

                    log.info("클라이언트에게 이미지 URL 전송 완료: {}, memberId: {}", taskId, memberId);

                    sseEmitterRepository.removeTaskMapping(taskId);
                } catch (Exception e) {
                    log.error("SSE 이벤트 전송 중 오류: {}", e.getMessage(), e);
                    sseEmitterRepository.remove(memberIdStr);
                }
            } else {
                log.warn("해당 사용자 ID에 대한 SSE 연결이 없습니다: {}, taskId: {}", memberId, taskId);
            }
        } catch (Exception e) {
            log.error("웹훅 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
