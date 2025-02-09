package com.ll.demo03.domain.prompt.service;

import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.prompt.entity.Prompt;
import com.ll.demo03.domain.prompt.repository.PromptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromptService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate;
    private final PromptRepository promptRepository;

    public String sendToGpt(Map<String, String> data, Member member) {
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
        String response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();

        // 프롬프트 저장
        Prompt prompt = new Prompt();
        prompt.setMember(member);
        prompt.setStyle(data.get("style"));
        prompt.setObject(data.get("object"));
        prompt.setPrompt(response);
        promptRepository.save(prompt);

        return response;
    }
}
