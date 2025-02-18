package com.ll.demo03.domain.image.service;

import com.ll.demo03.domain.referenceImage.service.ReferenceImageService;
import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mashape.unirest.http.HttpResponse;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

    @Value("${piapi.api.key}")
    private String piApiKey;

    @Value("${r2.bucket}")
    private String bucket;

    private final RestTemplate restTemplate;
    private final S3Client s3Client;
    private final ReferenceImageService referenceImageService;

    // 이미지 생성 (PiAPI 호출)
    // 이미지 생성 (PiAPI 호출)
    public String createImage(String prompt, String ratio, String referenceImage, String webhook) {
        try {
            System.out.println("prompt: " + prompt);
            if(referenceImage !=null ){
                prompt = prompt + " --sref " + referenceImage;
            }
            // 헤더 설정
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

    // 이미지 상태 확인
    public String checkTaskStatus(String taskId) {
        String url = "https://api.piapi.ai/api/v1/task/" + taskId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", piApiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();

        // Extract status and image_url from response (pseudo-code)
        String status = extractStatus(response);
        if ("completed".equals(status)) {
            return extractImageUrl(response);
        } else {
            return null;
        }
    }

    // 이미지 업스케일링
    public String upscale(String originTaskId, int index) {
        String url = "https://api.piapi.ai/api/v1/task";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", piApiKey);
        headers.set("Content-Type", "application/json");

        String body = "{\n" +
                "  \"model\": \"midjourney\",\n" +
                "  \"task_type\": \"upscale\",\n" +
                "  \"input\": {\n" +
                "    \"origin_task_id\": \"" + originTaskId + "\",\n" +
                "    \"index\": \"" + index + "\"\n" +
                "  }\n" +
                "}";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        String response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
        // Extract task_id from response (pseudo-code)
        return extractTaskId(response);
    }

    // 데이터베이스 처리
    public void saveImageData(Map<String, String> data) {
        // 예시: Supabase 대신 Spring Data JPA를 사용할 수 있음
        // UserImage userImage = new UserImage(data.get("style"), data.get("object"), data.get("image_url"));
        // userImageRepository.save(userImage);
    }

    private String extractTaskId(String response) {
        // JSON 파싱을 통해 task_id 추출하는 코드
        return "some_task_id"; // 예시
    }

    private String extractStatus(String response) {
        // JSON 파싱을 통해 상태 추출하는 코드
        return "completed"; // 예시
    }

    private String extractImageUrl(String response) {
        // JSON 파싱을 통해 이미지 URL 추출하는 코드
        return "image_url"; // 예시
    }
}
