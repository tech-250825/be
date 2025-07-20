package com.ll.demo03.imageTask.service;


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

        ImageTaskRequest newRequest = ImageTask.updatePrompt(request, network);
        ImageQueueRequest imageQueueRequest = ImageTask.toImageQueueRequest(newRequest, member);
        imageMessageProducer.sendImageCreationMessage(imageQueueRequest);
    }

    @Override
    public void processImageCreationTransactional(ImageTaskInitiate message) {

        Long memberId = message.getCreatorId();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));;
        member.decreaseCredit(1);

        ImageTask imageTask = ImageTask.from(member, message);
        ImageTask saved = imageTaskRepository.save(imageTask);
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
