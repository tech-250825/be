package com.ll.demo03.domain.upscaledTask.service;

import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.HttpResponse;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UpscaleTaskService {

    @Value("${piapi.api.key}")
    private String piApiKey;

    @Value("${r2.bucket}")
    private String bucket;


    public String createUpscaleImage(String taskId, String imageIndex,  String webhook) {
        try {
            log.info("Creating upscale image with taskId: {}, imageIndex: {}, webhook: {}", taskId, imageIndex, webhook);
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
