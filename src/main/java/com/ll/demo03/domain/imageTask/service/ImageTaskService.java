package com.ll.demo03.domain.imageTask.service;


import com.ll.demo03.domain.imageTask.entity.ImageTask;
import com.ll.demo03.domain.imageTask.repository.ImageTaskRepository;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.repository.MemberRepository;
import com.ll.demo03.domain.videoTask.dto.VideoMessageRequest;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.HttpResponse;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ImageTaskService {

    @Value("${runpod.api.key}")
    private String runpodApiKey;

    private final ImageTaskRepository imageTaskRepository;
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;

    public String createImage(Long taskId, String lora, String prompt, String webhook) {
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
              "lora": "%s"
            }
          }
        }
        """, webhook, taskId, prompt, lora);

            HttpResponse<String> response = Unirest.post("https://api.runpod.ai/v2/xsrpswvmyjpmhw/run")
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


    public void processImageCreationTransactional(VideoMessageRequest message, String webhookUrl) {
        Long memberId = message.getMemberId();
        Member member = memberRepository.getById(memberId);

        member.setCredit(member.getCredit() - 1);
        memberRepository.save(member);

        ImageTask imageTask = saveImageTask(message.getLora(), message.getPrompt(), memberId);
        Long taskId = imageTask.getId();
        log.info("taskId={}", taskId);

        redisTemplate.opsForList().rightPush("image:queue", String.valueOf(taskId));

        String response = createImage(
                taskId,
                message.getLora(),
                message.getPrompt(),
                webhookUrl + "/api/images/webhook"
        );
        // 필요시 log나 SSE 이벤트
    }

    public ImageTask saveImageTask(String lora, String prompt, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));

        ImageTask imageTask = new ImageTask();
        imageTask.setPrompt(prompt);
        imageTask.setLora(lora);
        imageTask.setMember(member);
        imageTask.setStatus("IN_PROGRESS");

        ImageTask saved = imageTaskRepository.save(imageTask);
        return saved;
    }
}
