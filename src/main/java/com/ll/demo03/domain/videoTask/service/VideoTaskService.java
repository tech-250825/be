package com.ll.demo03.domain.imageTask.service;


import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.imageTask.dto.TaskOrImageResponse;
import com.ll.demo03.domain.imageTask.entity.ImageTask;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.repository.MemberRepository;
import com.ll.demo03.domain.videoTask.dto.TaskOrVideoResponse;
import com.ll.demo03.domain.videoTask.dto.VideoMessageRequest;
import com.ll.demo03.domain.videoTask.entity.VideoTask;
import com.ll.demo03.domain.videoTask.repository.VideoTaskRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.HttpResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class VideoTaskService {

    private final VideoTaskRepository videoTaskRepository;
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;
    private final ImageRepository imageRepository;

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

    public PageResponse<List<TaskOrVideoResponse>> getMyTasks(Member member, CursorBasedPageable pageable) {
        Slice<VideoTask> taskPage;

        // 1. 커서 방향에 따른 처리
        if (!pageable.hasCursors()) {
            // 첫 페이지 요청 (내림차순 정렬)
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);
            taskPage = videoTaskRepository.findByMember(member, pageRequest);
        } else if (pageable.hasPrevPageCursor()) {
            // 이전 페이지 요청 (createdAt > cursor)
            String cursorValue = pageable.getDecodedCursor(pageable.getPrevPageCursor());
            LocalDateTime cursorCreatedAt = LocalDateTime.parse(cursorValue);

            Specification<VideoTask> spec = (root, query, cb) -> cb.and(
                    cb.equal(root.get("member"), member),
                    cb.greaterThan(root.get("createdAt"), cursorCreatedAt)
            );

            Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);
            taskPage = videoTaskRepository.findAll(spec, pageRequest);

            // createdAt 기준 최신순을 유지하려면 역순 정렬
            List<VideoTask> reversed = new ArrayList<>(taskPage.getContent());
            Collections.reverse(reversed);
            taskPage = new SliceImpl<>(reversed, pageRequest, taskPage.hasNext());
        } else {
            // 다음 페이지 요청 (createdAt < cursor)
            String cursorValue = pageable.getDecodedCursor(pageable.getNextPageCursor());
            LocalDateTime cursorCreatedAt = LocalDateTime.parse(cursorValue);

            Specification<VideoTask> spec = (root, query, cb) -> cb.and(
                    cb.equal(root.get("member"), member),
                    cb.lessThan(root.get("createdAt"), cursorCreatedAt)
            );

            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);
            taskPage = videoTaskRepository.findAll(spec, pageRequest);
        }

        // 2. 결과가 없을 경우 빈 응답
        if (!taskPage.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        // 3. Task 또는 Image로 응답 리스트 생성
        List<TaskOrVideoResponse> responseList = new ArrayList<>();
        List<VideoTask> taskList = taskPage.getContent();

        for (VideoTask task : taskList) {
            if ("IN_PROGRESS".equals(task.getStatus())) {
                responseList.add(TaskOrVideoResponse.fromTask(task));
            } else if ("COMPLETED".equals(task.getStatus())) {
                // N+1 발생 가능 → 성능 중요시하면 batch 조회 방식 추천
                List<Image> images = imageRepository.findByVideoTask(task);
                for (Image image : images) {
                    responseList.add(TaskOrVideoResponse.fromImage(task, image));
                }
            }
        }

        // 4. 커서 생성
        VideoTask first = taskList.get(0);
        VideoTask last = taskList.get(taskList.size() - 1);

        String prevCursor = pageable.getEncodedCursor(
                first.getCreatedAt().toString(),
                videoTaskRepository.existsByMemberAndCreatedAtGreaterThan(member, first.getCreatedAt())
        );

        String nextCursor = pageable.getEncodedCursor(
                last.getCreatedAt().toString(),
                videoTaskRepository.existsByMemberAndCreatedAtLessThan(member, last.getCreatedAt())
        );

        return new PageResponse<>(responseList, prevCursor, nextCursor);
    }
}