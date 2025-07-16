package com.ll.demo03.domain.imageTask.service;


import com.ll.demo03.domain.folder.dto.FolderImageResponse;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.imageTask.dto.ImageMessageRequest;
import com.ll.demo03.domain.imageTask.dto.TaskOrImageResponse;
import com.ll.demo03.domain.imageTask.entity.ImageTask;
import com.ll.demo03.domain.imageTask.repository.ImageTaskRepository;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.repository.MemberRepository;
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
import org.springframework.http.ResponseEntity;
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
public class ImageTaskService {

    @Value("${runpod.api.key}")
    private String runpodApiKey;

    private final ImageTaskRepository imageTaskRepository;
    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;

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

            HttpResponse<String> response = Unirest.post("https://api.runpod.ai/v2/ldkbvglhy10oq4/run")
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


    public void processImageCreationTransactional(ImageMessageRequest message, String webhookUrl) {
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

    public PageResponse<List<TaskOrImageResponse>> getMyTasks(Member member, CursorBasedPageable pageable) {
        Slice<ImageTask> taskPage;

        // 1. 커서 방향에 따른 처리
        if (!pageable.hasCursors()) {
            // 첫 페이지 요청 (내림차순 정렬)
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);
            taskPage = imageTaskRepository.findByMember(member, pageRequest);
        } else if (pageable.hasPrevPageCursor()) {
            // 이전 페이지 요청 (createdAt > cursor)
            String cursorValue = pageable.getDecodedCursor(pageable.getPrevPageCursor());
            LocalDateTime cursorCreatedAt = LocalDateTime.parse(cursorValue);

            Specification<ImageTask> spec = (root, query, cb) -> cb.and(
                    cb.equal(root.get("member"), member),
                    cb.greaterThan(root.get("createdAt"), cursorCreatedAt)
            );

            Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);
            taskPage = imageTaskRepository.findAll(spec, pageRequest);

            // createdAt 기준 최신순을 유지하려면 역순 정렬
            List<ImageTask> reversed = new ArrayList<>(taskPage.getContent());
            Collections.reverse(reversed);
            taskPage = new SliceImpl<>(reversed, pageRequest, taskPage.hasNext());
        } else {
            // 다음 페이지 요청 (createdAt < cursor)
            String cursorValue = pageable.getDecodedCursor(pageable.getNextPageCursor());
            LocalDateTime cursorCreatedAt = LocalDateTime.parse(cursorValue);

            Specification<ImageTask> spec = (root, query, cb) -> cb.and(
                    cb.equal(root.get("member"), member),
                    cb.lessThan(root.get("createdAt"), cursorCreatedAt)
            );

            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
            PageRequest pageRequest = PageRequest.of(0, pageable.getSize(), sort);
            taskPage = imageTaskRepository.findAll(spec, pageRequest);
        }

        // 2. 결과가 없을 경우 빈 응답
        if (!taskPage.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        // 3. Task 또는 Image로 응답 리스트 생성
        List<TaskOrImageResponse> responseList = new ArrayList<>();
        List<ImageTask> taskList = taskPage.getContent();

        for (ImageTask task : taskList) {
            if ("IN_PROGRESS".equals(task.getStatus())) {
                responseList.add(TaskOrImageResponse.fromTask(task));
            } else if ("COMPLETED".equals(task.getStatus())) {
                // N+1 발생 가능 → 성능 중요시하면 batch 조회 방식 추천
                List<Image> images = imageRepository.findByImageTask(task);
                for (Image image : images) {
                    responseList.add(TaskOrImageResponse.fromImage(task, image));
                }
            }
        }

        // 4. 커서 생성
        ImageTask first = taskList.get(0);
        ImageTask last = taskList.get(taskList.size() - 1);

        String prevCursor = pageable.getEncodedCursor(
                first.getCreatedAt().toString(),
                imageTaskRepository.existsByMemberAndCreatedAtGreaterThan(member, first.getCreatedAt())
        );

        String nextCursor = pageable.getEncodedCursor(
                last.getCreatedAt().toString(),
                imageTaskRepository.existsByMemberAndCreatedAtLessThan(member, last.getCreatedAt())
        );

        return new PageResponse<>(responseList, prevCursor, nextCursor);
    }

}
