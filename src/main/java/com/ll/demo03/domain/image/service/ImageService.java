package com.ll.demo03.domain.image.service;

import com.ll.demo03.global.dto.GlobalResponse;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mashape.unirest.http.HttpResponse;
import java.util.Map;

@Service
public class ImageService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${piapi.api.key}")
    private String piApiKey;

    private final RestTemplate restTemplate;

    @Autowired
    public ImageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String sendToGpt(Map<String, String> data) {
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openAiApiKey);
        headers.set("Content-Type", "application/json");

        // 이스케이프 문자 추가 및 JSON 문법 수정
        String body = "{\n" +
                "  \"model\": \"gpt-4o-mini\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"In english, You are an artist. You are going to describe an illustration\\n" +
                "                that meets the user's demand. Don't over-imagine. Use specific wording (ex, light and shadow texture, flat colors, cell shading and ink lines). The style description needs to go first and last in the prompt (ex, retro anime, japanese illustration), or use the director or\\n" +
                "                artist's name related to the style (ex, Ghibli Studio, Hayao Miyazaki, Jeremy Geddes, Junji Ito, Naoko Takeuchi, ...), or specific style (ex: retro anime -> vhs effect, grainy texture, 80s anime, motion blur, realistic -> 4k). If it's animation or character,\\n" +
                "                write simply, in 1~2 sentences. If the user wants a pretty girl, add 'in the style of guweiz'. Don't use korean.\\n" +
                "                If it's realism, describe pose, layout, composition, add 4k. If the user seems to want retro anime, add --niji 5 at the end of the prompt.\"},\n" +
                "    {\"role\": \"user\", \"content\": \"Here is the user's demand: " + data.get("style") + " " + data.get("object") + "\"}\n" +
                "  ]\n" +
                "}";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
    }


    // 이미지 생성 (PiAPI 호출)
    public String createImage(String prompt, String ratio) {
        try {
            System.out.println(prompt);
            System.out.println(ratio);
            // 헤더 설정
            Unirest.setTimeouts(0, 0);
            HttpResponse<String> response = (HttpResponse<String>) Unirest.post("https://api.piapi.ai/api/v1/task")
                    .header("x-api-key", piApiKey)
                    .header("Content-Type", "application/json")
                    .body("{\r\n    \"model\": \"midjourney\",\r\n    \"task_type\": \"imagine\",\r\n    \"input\": {\r\n        \"prompt\": \"" + prompt + "\",\r\n        \"aspect_ratio\": \"" + ratio + "\",\r\n        \"process_mode\": \"fast\",\r\n        \"skip_prompt_check\": false,\r\n        \"bot_id\": 0\r\n    },\r\n    \"config\": {\r\n        \"service_mode\": \"\",\r\n        \"webhook_config\": {\r\n            \"endpoint\": \"https://webhook.site/\",\r\n            \"secret\": \"123456\"\r\n        }\r\n    }\r\n}")
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
        return "https://example.com/image.jpg"; // 예시
    }
}
