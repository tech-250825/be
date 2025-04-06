package com.ll.demo03.domain.taskProcessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.repository.MemberRepository;
import com.ll.demo03.domain.sse.repository.SseEmitterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class TaskProcessingService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SseEmitterRepository sseEmitterRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendSseStatusEvent(Long memberId, String taskId, String statusMessage) {
        try {
            SseEmitter emitter = sseEmitterRepository.get(String.valueOf(memberId));
            if (emitter != null) {
                Map<String, Object> completeData = new HashMap<>();
                completeData.put("status", statusMessage);
                completeData.put("memberId", memberId);
                completeData.put("taskId", taskId);

                emitter.send(SseEmitter.event()
                        .name("status")
                        .data(objectMapper.writeValueAsString(completeData)));

                sseEmitterRepository.save(taskId, emitter);

                log.info("SSE 상태 메시지 전송 완료: {}, memberId: {}", taskId, memberId);
            } else {
                log.warn("SSE 연결을 찾을 수 없음: memberId={}", memberId);
            }
        } catch (Exception e) {
            log.error("SSE 상태 메시지 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public String extractTaskIdFromResponse(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            return rootNode.path("data").path("task_id").asText();
        } catch (Exception e) {
            log.error("응답에서 task_id 추출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("Task ID 추출 실패", e);
        }
    }

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("멤버를 찾지 못했습니다.: " + memberId));
    }
}
