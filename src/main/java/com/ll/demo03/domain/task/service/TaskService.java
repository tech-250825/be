package com.ll.demo03.domain.task.service;

import com.ll.demo03.domain.referenceImage.service.ReferenceImageService;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mashape.unirest.http.HttpResponse;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    @Value("${piapi.api.key}")
    private String piApiKey;

    @Value("${r2.bucket}")
    private String bucket;

    private final RestTemplate restTemplate;
    private final S3Client s3Client;
    private final ReferenceImageService referenceImageService;

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


    // 이미지 업스케일링
//    public String upscale(String originTaskId, int index) {
//        String url = "https://api.piapi.ai/api/v1/task";
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("x-api-key", piApiKey);
//        headers.set("Content-Type", "application/json");
//
//        String body = "{\n" +
//                "  \"model\": \"midjourney\",\n" +
//                "  \"task_type\": \"upscale\",\n" +
//                "  \"input\": {\n" +
//                "    \"origin_task_id\": \"" + originTaskId + "\",\n" +
//                "    \"index\": \"" + index + "\"\n" +
//                "  }\n" +
//                "}";
//
//        HttpEntity<String> entity = new HttpEntity<>(body, headers);
//        String response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
//        return response.getBody();
//    }


}
