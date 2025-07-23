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

    public String modifyPrompt( String lora, String prompt) {
        String openAiUrl = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        try {
            Map<String, Object> systemMessage = Map.of(
                    "role", "system",
                    "content", "If the user's input is in Korean, translate it into natural English. " +
                            "If the input is already in English, do not change it. " +
                            "Do not add any style or artistic interpretation — just translate or preserve as is."
            );

            Map<String, Object> userMessage = Map.of(
                    "role", "user",
                    "content", prompt
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

        } catch (Exception e) {
            log.error("OpenAI API 요청 실패: ", e);
            return prompt;
        }
    }

    public String createImage(Long taskId, String lora, String prompt, String webhook) {
        try {
            Unirest.setTimeouts(0, 0);

            String jsonBody = String.format("""
        {
          "webhook": "%s",
          "input": {
            "workflow": "illustrious_image",
            "payload": {
              "task_id": %d,
              "positive_prompt": "%s",
              "lora": "%s"
            }
          }
        }
        """, webhook, taskId, prompt, lora);

            HttpResponse<String> response = Unirest.post("https://api.runpod.ai/v2/dv20fz38sl3z7t//run")
                    .header("accept", "application/json")
                    .header("authorization", runpodApiKey)
                    .header("content-type", "application/json")
                    .body(jsonBody)
                    .asString();

            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public String createVideo(Long taskId, String lora, String prompt, int width, int height, int numFrames , String webhook) {
        try {
            Unirest.setTimeouts(0, 0);

            String jsonBody = String.format("""
           {
          "webhook": "%s",
          "input": {
            "workflow": "wan_video",
            "payload": {
              "task_id": %d,
              "positive_prompt": "%s",
              "lora": "%s",
              "width" : %d,
              "height" : %d, 
              "num_frames" : %d
            }
          }
        }
        """, webhook, taskId, prompt , lora, width, height, numFrames);

            HttpResponse<String> response = Unirest.post("https://api.runpod.ai/v2/dv20fz38sl3z7t/run")
                    .header("accept", "application/json")
                    .header("authorization", runpodApiKey)
                    .header("content-type", "application/json")
                    .body(jsonBody)
                    .asString();

            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public String createVideo(Long taskId, String lora, String prompt, String url, int width, int height, int numFrames , String webhook) {
        try {
            Unirest.setTimeouts(0, 0);

            String jsonBody = String.format("""
           {
          "webhook": "%s",
          "input": {
            "workflow": "wan_video",
            "payload": {
              "task_id": %d,
              "positive_prompt": "%s",
              "image_url": "%s",
              "width" : %d,
              "height" : %d, 
              "num_frames" : %d
            }
          }
        }
        """, webhook, taskId, prompt , url, width, height, numFrames);

            HttpResponse<String> response = Unirest.post("https://api.runpod.ai/v2/dgxtzrxjg40wpi/run")
                    .header("accept", "application/json")
                    .header("authorization", runpodApiKey)
                    .header("content-type", "application/json")
                    .body(jsonBody)
                    .asString();

            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
