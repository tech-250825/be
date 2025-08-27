package com.ll.demo03.global.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.port.AlertService;
import com.ll.demo03.global.port.Network;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Service
public class NetworkImpl implements Network {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final AlertService alertService;

    @Value("${runpod.api.key}")
    private String runpodApiKey;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public String createImageFaceDetailer(Long taskId, String checkpoint, String lora, String prompt, String negativePrompt, int width, int height, String webhook) {
        try {
            Unirest.setTimeouts(0, 0);

            Map<String, Object> payload = Map.of(
                    "task_id", taskId,
                    "positive_prompt", prompt,
                    "negative_prompt" , negativePrompt,
                    "checkpoint", checkpoint,
                    "lora", lora,
                    "width", width,
                    "height", height
            );

            Map<String, Object> input = Map.of(
                    "workflow", "face_detailer",
                    "payload", payload
            );

            Map<String, Object> requestBody = Map.of(
                    "webhook", webhook,
                    "input", input
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpResponse<String> response = Unirest.post("https://api.runpod.ai/v2/hqswi0mxvsd2kt/run")
                    .header("accept", "application/json")
                    .header("authorization", runpodApiKey)
                    .header("content-type", "application/json")
                    .body(jsonBody)
                    .asString();

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public String createImagePlain (Long taskId, String checkpoint, String prompt, String negativePrompt, int width, int height, String webhook) {

        try {
            Unirest.setTimeouts(0, 0);
            Map<String, Object> payload = Map.of(
                    "task_id", taskId,
                    "positive_prompt", prompt,
                    "negative_prompt" , negativePrompt,
                    "checkpoint", checkpoint,
                    "width", width,
                    "height", height
            );
            Map<String, Object> input = Map.of(
                    "workflow", "SDXL_workflow_no_lora",
                    "payload", payload
            );

            Map<String, Object> requestBody = Map.of(
                    "webhook", webhook,
                    "input", input
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpResponse<String> response = Unirest.post("https://api.runpod.ai/v2/yoo61r5n5h2vdy/run")
                    .header("accept", "application/json")
                    .header("authorization", runpodApiKey)

                    .header("content-type", "application/json")
                    .body(jsonBody)
                    .asString();

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public String createI2I(Long taskId, String imageUrl, String prompt, String webhook) {
        try {
            Unirest.setTimeouts(0, 0);

            Map<String, Object> payload = Map.of(
                    "task_id", taskId,
                    "positive_prompt", prompt,
                    "image_url", imageUrl
            );

            Map<String, Object> input = Map.of(
                    "workflow", "img2img",
                    "payload", payload
            );

            Map<String, Object> requestBody = Map.of(
                    "webhook", webhook,
                    "input", input
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpResponse<String> response = Unirest.post("https://api.runpod.ai/v2/5ur6tlm9ltg8hu/run")
                    .header("accept", "application/json")
                    .header("authorization", runpodApiKey)
                    .header("content-type", "application/json")
                    .body(jsonBody)
                    .asString();

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    public String createT2VVideo(Long taskId, String lora, String prompt, int width, int height, int numFrames , String webhook) {
        try {
            Unirest.setTimeouts(0, 0);

            Map<String, Object> payload = Map.of(
                    "task_id", taskId,
                    "positive_prompt", prompt,
                    "lora", lora,
                    "width", width,
                    "height", height,
                    "num_frames", numFrames
            );

            Map<String, Object> input = Map.of(
                    "workflow", "wan_video",
                    "payload", payload
            );

            Map<String, Object> requestBody = Map.of(
                    "webhook", webhook,
                    "input", input
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpResponse<String> response = Unirest.post("https://api.runpod.ai/v2/dv20fz38sl3z7t/run")
                    .header("accept", "application/json")
                    .header("authorization", runpodApiKey)
                    .header("content-type", "application/json")
                    .body(jsonBody)
                    .asString();

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public String createI2VVideo(Long taskId, String prompt, String url, int width, int height, int numFrames , String webhook) {
        try {
            Unirest.setTimeouts(0, 0);

            Map<String, Object> payload = Map.of(
                    "task_id", taskId,
                    "positive_prompt", prompt,
                    "image_url", url,
                    "width", width,
                    "height", height,
                    "num_frames", numFrames
            );

            Map<String, Object> input = Map.of(
                    "workflow", "wan_i2v",
                    "payload", payload
            );

            Map<String, Object> requestBody = Map.of(
                    "webhook", webhook,
                    "input", input
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpResponse<String> response = Unirest.post("https://api.runpod.ai/v2/dgxtzrxjg40wpi/run")
                    .header("accept", "application/json")
                    .header("authorization", runpodApiKey)
                    .header("content-type", "application/json")
                    .body(jsonBody)
                    .asString();

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
