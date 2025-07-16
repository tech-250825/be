package com.ll.demo03.domain.imageTask.service;


import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.repository.MemberRepository;
import com.ll.demo03.domain.videoTask.dto.VideoMessageRequest;
import com.ll.demo03.domain.videoTask.entity.VideoTask;
import com.ll.demo03.domain.videoTask.repository.VideoTaskRepository;
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
public class VideoTaskService {

    private final VideoTaskRepository videoTaskRepository;
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;

    @Value("${runpod.api.key}")
    private String runpodApiKey;


    public String createVideo(Long taskId, String lora, String prompt, String webhook) {
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

            HttpResponse<String> response = Unirest.post("https://api.runpod.ai/v2/vmyn0177mpa0ev/run")
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


    public void processVideoCreationTransactional(VideoMessageRequest message, String webhookUrl) {
        Long memberId = message.getMemberId();
        Member member = memberRepository.getById(memberId);

        member.setCredit(member.getCredit() - 1);
        memberRepository.save(member);

        VideoTask videoTask = saveVideoTask(message.getLora(), message.getPrompt(), memberId);
        Long taskId = videoTask.getId();
        log.info("taskId={}", taskId);

        redisTemplate.opsForList().rightPush("video:queue", String.valueOf(taskId));

        String response = createVideo(
                taskId,
                message.getLora(),
                message.getPrompt(),
                webhookUrl + "/api/videos/webhook"
        );
        // 필요시 log나 SSE 이벤트
    }

    public VideoTask saveVideoTask( String lora, String prompt, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));

        VideoTask videoTask = new VideoTask();
        videoTask.setPrompt(prompt);
        videoTask.setLora(lora);
        videoTask.setMember(member);
        videoTask.setStatus("IN_PROGRESS");

        VideoTask saved = videoTaskRepository.save(videoTask);
        return saved;
    }
}