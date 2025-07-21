package com.ll.demo03.videoTask.service;


import com.ll.demo03.global.domain.Status;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
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
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER)); //이렇게 영속화 시켜야함
        creator.decreaseCredit(1); //@AuthenticationPrincipal PrincipalDetails 에서 꺼낸 member 객체는 JPA에 영속되어있지 않으므로 decreaseCredit해도 JPA가 트랜잭션 커밋 시점에 알아서 update 쿼리를 날리지 않는다 .

        VideoTaskRequest newRequest = VideoTask.updatePrompt(request, network);
        VideoQueueRequest videoQueueRequest = VideoTask.toQueueRequest(newRequest, member);
        videoMessageProducer.sendVideoCreationMessage(videoQueueRequest);
        memberRepository.save(creator);
    }

    @Override
    public void process(VideoQueueRequest message) { //이건 비동기므로 에러가 나도 /api/videos/create에서 에러 반환 못받는다ㅜㅜ 해결책 고안할 것 .

        Member member = memberRepository.findById(message.getMemberId()).orElseThrow(() -> new EntityNotFoundException("Member not found")); //rabbitmq TLS화하여 보안설정 하기

        VideoTask task = VideoTask.from(member, message);
        task = task.updateStatus(Status.IN_PROGRESS, null);
        VideoTask saved = videoTaskRepository.save(task);
        Long taskId = saved.getId();

        redisService.pushToQueue("video", taskId);

        network.createVideo(
                taskId,
                message.getLora(),
                message.getPrompt(),
                message.getWidth(),
                message.getHeight(),
                message.getNumFrames(),
                webhookUrl + "/api/videos/webhook"
        );
    }

    @Override
    public PageResponse<List<TaskOrVideoResponse>> getMyTasks(Member member, CursorBasedPageable pageable) {
        return paginationService.getPagedContent(member, pageable, paginationStrategy, responseConverter);
    }

}