package com.ll.demo03.imageTask.service;


import com.ll.demo03.global.domain.Status;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.infrastructure.MessageProducerImpl;
import com.ll.demo03.global.port.CursorPaginationService;
import com.ll.demo03.global.port.RedisService;
import com.ll.demo03.imageTask.controller.port.ImageTaskService;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import com.ll.demo03.imageTask.controller.request.ImageTaskRequest;
import com.ll.demo03.imageTask.controller.response.TaskOrImageResponse;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.domain.ImageTaskInitiate;
import com.ll.demo03.global.port.Network;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
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
public class ImageTaskServiceImpl implements ImageTaskService {

    private final ImageTaskRepository imageTaskRepository;
    private final MemberRepository memberRepository;
    private final RedisService redisService;
    private final Network network;
    private final MessageProducerImpl imageMessageProducer;
    private final CursorPaginationService paginationService;
    private final ImageTaskPaginationStrategy paginationStrategy;
    private final ImageTaskResponseConverter responseConverter;

    @Value("${custom.webhook-url}")
    private String webhookUrl;

    @Override
    public void initate(ImageTaskRequest request, Member member){
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER)); //이렇게 영속화 시켜야함
        creator.decreaseCredit(1); //@AuthenticationPrincipal PrincipalDetails 에서 꺼낸 member 객체는 JPA에 영속되어있지 않으므로 decreaseCredit해도 JPA가 트랜잭션 커밋 시점에 알아서 update 쿼리를 날리지 않는다 .

        ImageTaskRequest newRequest = ImageTask.updatePrompt(request, network);
        ImageQueueRequest imageQueueRequest = ImageTask.toImageQueueRequest(newRequest, member);
        imageMessageProducer.sendImageCreationMessage(imageQueueRequest);
        memberRepository.save(creator);
    }

    @Override
    public void processImageCreationTransactional(ImageQueueRequest message) { //비동기 에러나면 어칼껀데

        Member member = memberRepository.findById(message.getMemberId()).orElseThrow(() -> new EntityNotFoundException("Member not found")); //rabbitmq TLS화하여 보안설정 하기

        ImageTask task = ImageTask.from(member, message);
        task = task.updateStatus(Status.IN_PROGRESS, null);
        ImageTask saved = imageTaskRepository.save(task);
        Long taskId = saved.getId();

        redisService.pushToQueue("image", taskId);

        network.createImage(
                taskId,
                message.getLora(),
                message.getPrompt(),
                webhookUrl + "/api/images/webhook"
        );
    }

    @Override
    public PageResponse<List<TaskOrImageResponse>> getMyTasks(Member member, CursorBasedPageable pageable) {
        return paginationService.getPagedContent(member, pageable, paginationStrategy, responseConverter);
    }

}
