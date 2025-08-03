package com.ll.demo03.imageTask.service;


import com.ll.demo03.global.domain.Status;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.infrastructure.MessageProducerImpl;
import com.ll.demo03.global.port.CursorPaginationService;
import com.ll.demo03.global.port.MessageProducer;
import com.ll.demo03.global.port.RedisService;
import com.ll.demo03.imageTask.controller.port.ImageTaskService;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import com.ll.demo03.imageTask.controller.request.ImageTaskRequest;
import com.ll.demo03.imageTask.controller.response.TaskOrImageResponse;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.global.port.Network;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.lora.controller.port.LoraService;
import com.ll.demo03.lora.domain.Lora;
import com.ll.demo03.lora.service.port.LoraRepository;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ImageTaskServiceImpl implements ImageTaskService {

    private final ImageTaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final RedisService redisService;
    private final Network network;
    private final MessageProducer messageProducer;
    private final CursorPaginationService paginationService;
    private final ImageTaskPaginationStrategy paginationStrategy;
    private final ImageTaskResponseConverter responseConverter;
    private final LoraRepository loraRepository;
    private final LoraService loraService;
    private final UGCRepository ugcRepository;

    @Value("${custom.webhook-url}")
    private String webhookUrl;


    @Override
    public void initate(ImageTaskRequest request, Member member){
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost();
        creator.decreaseCredit(creditCost);

        Lora lora = loraRepository.findById(request.getLoraId()).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        
        String newPrompt = loraService.addTriggerWord(lora.getId(), request.getPrompt());
        
        ImageQueueRequest imageQueueRequest = ImageTask.toImageQueueRequest(request, lora.getModelName(), newPrompt, member);
        messageProducer.sendImageCreationMessage(imageQueueRequest);
        memberRepository.save(creator);
    }

    @Override
    public void processImageCreationTransactional(ImageQueueRequest message) { //비동기 에러나면 어칼껀데

        Member member = memberRepository.findById(message.getMemberId()).orElseThrow(() -> new EntityNotFoundException("Member not found")); //rabbitmq TLS화하여 보안설정 하기

        ImageTask task = ImageTask.from(member, message);
        task = task.updateStatus(Status.IN_PROGRESS, null);
        ImageTask saved = taskRepository.save(task);
        Long taskId = saved.getId();

        redisService.pushToQueue("image", taskId);

        network.createImage(
                taskId,
                message.getLora(),
                message.getPrompt(),
                message.getWidth(),
                message.getHeight(),
                webhookUrl + "/api/images/webhook"
        );
    }

    @Override
    public PageResponse<List<TaskOrImageResponse>> getMyTasks(Member member, CursorBasedPageable pageable) {
        return paginationService.getPagedContent(member, pageable, paginationStrategy, responseConverter);
    }

    @Override
    public void delete(Long taskId) {
        ImageTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        List<UGC> ugcs = ugcRepository.findAllByImageTaskId(task.getId());
        ugcRepository.deleteAll(ugcs);

        taskRepository.delete(task);
    }
}
