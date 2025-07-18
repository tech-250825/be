package com.ll.demo03.videoTask.service;


import com.ll.demo03.global.port.CursorPaginationService;
import com.ll.demo03.global.port.MessageProducer;
import com.ll.demo03.global.port.Network;
import com.ll.demo03.global.port.RedisService;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
import com.ll.demo03.videoTask.controller.port.VideoTaskService;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.videoTask.controller.request.VideoQueueRequest;
import com.ll.demo03.videoTask.controller.request.VideoTaskRequest;
import com.ll.demo03.videoTask.controller.response.TaskOrVideoResponse;
import com.ll.demo03.videoTask.domain.VideoTask;
import com.ll.demo03.videoTask.domain.VideoTaskInitiate;
import com.ll.demo03.videoTask.service.port.VideoTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class VideoTaskServiceImpl implements VideoTaskService {

    private final VideoTaskRepository videoTaskRepository;
    private final MemberRepository memberRepository;
    private final RedisService redisService;
    private final Network network;
    private final MessageProducer videoMessageProducer;
    private final CursorPaginationService paginationService;
    private final VideoTaskPaginationStrategy paginationStrategy;
    private final VideoTaskResponseConverter responseConverter;

    @Value("${custom.webhook-url}")
    private String webhookUrl;

    @Override
    public void initate(VideoTaskRequest request, Member member){

        VideoTaskRequest newRequest = VideoTask.updatePrompt(request, network);
        VideoQueueRequest videoQueueRequest = VideoTask.toQueueRequest(newRequest, member);
        videoMessageProducer.sendVideoCreationMessage(videoQueueRequest);
    }

    @Override
    public void process(VideoTaskInitiate message) {

        Long memberId = message.getCreatorId();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));;
        member.decreaseCredit(1);

        VideoTask videoTask = VideoTask.from(member, message);
        VideoTask saved = videoTaskRepository.save(videoTask);
        Long taskId = saved.getId();

        redisService.pushToImageQueue(String.valueOf(taskId));

        network.createImage(
                taskId,
                message.getLora(),
                message.getPrompt(),
                webhookUrl + "/api/images/webhook"
        );
    }

    @Override
    public PageResponse<List<TaskOrVideoResponse>> getMyTasks(Member member, CursorBasedPageable pageable) {
        return paginationService.getPagedContent(member, pageable, paginationStrategy, responseConverter);
    }

}