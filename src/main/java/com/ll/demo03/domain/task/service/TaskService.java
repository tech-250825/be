package com.ll.demo03.domain.task.service;

import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import com.ll.demo03.domain.task.dto.AckInfo;
import com.ll.demo03.domain.task.repository.TaskRepository;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TaskService {

    @Value("${piapi.api.key}")
    private String piApiKey;

    @Value("${r2.bucket}")
    private String bucket;


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
}
