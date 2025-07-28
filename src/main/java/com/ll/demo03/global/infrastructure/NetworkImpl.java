package com.ll.demo03.global.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
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

    @Value("${runpod.api.key}")
    private String runpodApiKey;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public String modifyPrompt( String gptPrompt, String oldPrompt) {
        String openAiUrl = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        try {
            Map<String, Object> systemMessage = Map.of(
                    "role", "system",
                    "content", gptPrompt
            );

            Map<String, Object> userMessage = Map.of(
                    "role", "user",
                    "content", oldPrompt
            );

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "temperature", 0,
                    "messages", List.of(systemMessage, userMessage)
            );

            String jsonBody = objectMapper.writeValueAsString(body);
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(openAiUrl, HttpMethod.POST, requestEntity, String.class);
            String responseBody = responseEntity.getBody();

            JsonNode rootNode = objectMapper.readTree(responseBody);
            String result = rootNode.path("choices").get(0).path("message").path("content").asText().trim();

            log.info("OpenAI 프롬프트 개선 응답: {}", result);
            return result;

        }catch (Exception e) {
            log.error("OpenAI API 호출 실패", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public String createImage(Long taskId, String lora, String prompt, String webhook) {
        try {
            Unirest.setTimeouts(0, 0);

            Map<String, Object> payload = Map.of(
                    "task_id", taskId,
                    "positive_prompt", prompt,
                    "lora", lora
            );

            Map<String, Object> input = Map.of(
                    "workflow", "illustrious_image",
                    "payload", payload
            );

            Map<String, Object> requestBody = Map.of(
                    "webhook", webhook,
                    "input", input
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpResponse<String> response = Unirest.post("https://api.runpod.ai/v2/dv20fz38sl3z7t//run")
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
