package com.ll.demo03.domain.upscaledTask.service;

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
public class UpscaleTaskService {

    @Value("${piapi.api.key}")
    private String piApiKey;

    @Value("${r2.bucket}")
    private String bucket;

    private final RestTemplate restTemplate;
    private final S3Client s3Client;

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

}
