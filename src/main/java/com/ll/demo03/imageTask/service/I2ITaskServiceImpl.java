package com.ll.demo03.imageTask.service;

import com.ll.demo03.global.domain.Status;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.port.*;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.imageTask.controller.port.I2ITaskService;
import com.ll.demo03.imageTask.controller.request.I2ITask.I2IQueueRequest;
import com.ll.demo03.imageTask.controller.request.I2ITask.I2ITaskRequest;
import com.ll.demo03.imageTask.controller.request.I2ITask.I2ITaskRequestV2;
import com.ll.demo03.imageTask.controller.response.TaskOrI2IResponse;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
import com.ll.demo03.weight.controller.port.WeightService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class I2ITaskServiceImpl implements I2ITaskService {
    private final MemberRepository memberRepository;
    private final MessageProducer messageProducer;
    private final ImageTaskRepository taskRepository;
    private final Network network;
    private final WeightService weightService;
    private final S3Service s3Service;
    private final RedisService redisService;
    private final I2ITaskResponseConverter responseConverter;
    private final CursorPaginationService paginationService;
    private final I2ITaskPaginationStrategy paginationStrategy;

    @Value("${custom.webhook-url}")
    private String webhookUrl;

    @Override
    public void initate(I2ITaskRequest request, Member member){
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost();
        creator.decreaseCredit(creditCost);
        memberRepository.save(creator);

        String prompt = request.getPrompt();
        ImageTask task = ImageTask.from(member, request.getPrompt(), request.getImageUrl(), request.getResolutionProfile());
        task = task.updateStatus(Status.IN_PROGRESS, null);
        ImageTask saved = taskRepository.save(task);

        I2IQueueRequest queueRequest = ImageTask.toI2IQueueRequest(saved.getId(), request.getImageUrl(), prompt, member);
        messageProducer.sendCreationMessage(queueRequest);
    }

    @Override
    public void initate(I2ITaskRequestV2 request, Member member, MultipartFile image){
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost();
        creator.decreaseCredit(creditCost);
        memberRepository.save(creator);

        String url = s3Service.uploadFile(image);

        String prompt = request.getPrompt();

        ImageTask task = ImageTask.from(member, prompt, url, request.getResolutionProfile());
        task = task.updateStatus(Status.IN_PROGRESS, null);
        ImageTask saved = taskRepository.save(task);

        I2IQueueRequest queueRequest = ImageTask.toI2IQueueRequest(saved.getId(), url, prompt, member);
        messageProducer.sendCreationMessage(queueRequest);
    }

    @Override
    public void processCreationTransactional(I2IQueueRequest message) { //비동기 에러나면 어칼껀데

        redisService.pushToQueue("image", message.getTaskId());

        network.createI2I(
                message.getTaskId(),
                message.getPrompt(),
                message.getImageUrl(),
                webhookUrl + "/api/img2img/webhook"
        );
    }

    @Override
    public PageResponse<List<TaskOrI2IResponse>> getMyTasks(Member member, CursorBasedPageable pageable) {
        return paginationService.getPagedContent(member, pageable, paginationStrategy, responseConverter);
    }
}
