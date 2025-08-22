package com.ll.demo03.imageTask.service;


import com.ll.demo03.global.domain.Status;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.port.CursorPaginationService;
import com.ll.demo03.global.port.MessageProducer;
import com.ll.demo03.global.port.RedisService;
import com.ll.demo03.imageTask.controller.port.ImageTaskService;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import com.ll.demo03.imageTask.controller.request.ImageQueueV3Request;
import com.ll.demo03.imageTask.controller.request.ImageTaskRequest;
import com.ll.demo03.imageTask.controller.request.ImageTaskV3Request;
import com.ll.demo03.imageTask.controller.response.TaskOrImageResponse;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.global.port.Network;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.videoTask.domain.VideoTask;
import com.ll.demo03.weight.controller.port.WeightService;
import com.ll.demo03.weight.domain.Weight;
import com.ll.demo03.weight.service.port.WeightRepository;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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
    private final WeightRepository weightRepository;
    private final WeightService weightService;
    private final UGCRepository ugcRepository;

    @Value("${custom.webhook-url}")
    private String webhookUrl;

    @Override
    public void initate(ImageTaskRequest request, Member member){
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost();
        creator.decreaseCredit(creditCost);
        memberRepository.save(creator);

        Weight checkpoint = weightRepository.findById(request.getCheckpointId()).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        Weight lora = weightRepository.findById(request.getLoraId()).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        String prompt = request.getPrompt();
        boolean result = network.censorSoftPrompt(prompt);
        if (result== true) {throw new CustomException(ErrorCode.COMMUNITY_GUIDELINE_VIOLATION);}

        String gptPrompt = weightService.updatePrompt(lora.getId(), prompt);

        String newPrompt = weightService.addTriggerWord(lora.getId(), gptPrompt);

        ImageTask task = ImageTask.from(member, checkpoint, lora, request.getPrompt(),  newPrompt, request.getResolutionProfile());
        task = task.updateStatus(Status.IN_PROGRESS, null);
        ImageTask saved = taskRepository.save(task);

        ImageQueueRequest imageQueueRequest = ImageTask.toImageQueueRequest(saved.getId(), request, checkpoint.getModelName(), lora.getModelName(), newPrompt, lora.getNegativePrompt(), member);
        messageProducer.sendImageCreationMessage(imageQueueRequest);
    }


    @Override
    public void initateFaceDetailer(ImageTaskRequest request, Member member){
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost();
        creator.decreaseCredit(creditCost);
        memberRepository.save(creator);

        Weight checkpoint = weightRepository.findById(request.getCheckpointId()).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        Weight lora = weightRepository.findById(request.getLoraId()).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        String prompt = request.getPrompt();
        boolean result = network.censorSoftPrompt(prompt);
        if (result== true) {throw new CustomException(ErrorCode.COMMUNITY_GUIDELINE_VIOLATION);}

        String gptPrompt = weightService.updatePrompt(lora.getId(), prompt);

        String newPrompt = weightService.addTriggerWord(checkpoint.getId(), gptPrompt);

        ImageTask task = ImageTask.from(member, checkpoint, lora, request.getPrompt(), newPrompt, request.getResolutionProfile());
        task = task.updateStatus(Status.IN_PROGRESS, null);
        ImageTask saved = taskRepository.save(task);
        
        ImageQueueRequest imageQueueRequest = ImageTask.toImageQueueRequest(saved.getId(), request, checkpoint.getModelName(), lora.getModelName(), newPrompt, checkpoint.getNegativePrompt(), member);
        messageProducer.sendFaceDetailerCreationMessage(imageQueueRequest);
    }

    @Override
    public void initatePlainImage(ImageTaskV3Request request, Member member){
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost();
        creator.decreaseCredit(creditCost);
        memberRepository.save(creator);

        Weight checkpoint = weightRepository.findById(request.getCheckpointId()).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        String prompt = request.getPrompt();
        boolean result = network.censorSoftPrompt(prompt);
        if (result== true) {throw new CustomException(ErrorCode.COMMUNITY_GUIDELINE_VIOLATION);}

        String gptPrompt = weightService.updatePrompt(checkpoint.getId(), prompt);

        String newPrompt = weightService.addTriggerWord(checkpoint.getId(), gptPrompt);

        ImageTask task = ImageTask.from(member, checkpoint, request.getPrompt(), newPrompt, request.getResolutionProfile());
        task = task.updateStatus(Status.IN_PROGRESS, null);
        ImageTask saved = taskRepository.save(task);

        ImageQueueV3Request imageQueueRequest = ImageTask.toImageQueueRequest(saved.getId(), request, checkpoint.getModelName(), newPrompt, checkpoint.getNegativePrompt(), member);
        messageProducer.sendPlainCreationMessage(imageQueueRequest);
    }

    @Override
    public void initateNsfwFaceDetailer(ImageTaskRequest request, Member member){
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost();
        creator.decreaseCredit(creditCost);
        memberRepository.save(creator);

        Weight checkpoint = weightRepository.findById(request.getCheckpointId()).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        Weight lora = weightRepository.findById(request.getLoraId()).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        String newPrompt = weightService.addTriggerWord(checkpoint.getId(), request.getPrompt());

        ImageTask task = ImageTask.from(member, checkpoint, lora, request.getPrompt(), newPrompt, request.getResolutionProfile());
        task = task.updateStatus(Status.IN_PROGRESS, null);
        ImageTask saved = taskRepository.save(task);

        ImageQueueRequest imageQueueRequest = ImageTask.toImageQueueRequest(saved.getId(), request, checkpoint.getModelName(), lora.getModelName(), newPrompt, checkpoint.getNegativePrompt(), member);
        messageProducer.sendFaceDetailerCreationMessage(imageQueueRequest);
    }

    @Override
    public void initateNsfwPlainImage(ImageTaskV3Request request, Member member){
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost();
        creator.decreaseCredit(creditCost);
        memberRepository.save(creator);

        Weight checkpoint = weightRepository.findById(request.getCheckpointId()).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        String newPrompt = weightService.addTriggerWord(checkpoint.getId(), request.getPrompt());

        ImageTask task = ImageTask.from(member, checkpoint, request.getPrompt(), newPrompt, request.getResolutionProfile());
        task = task.updateStatus(Status.IN_PROGRESS, null);
        ImageTask saved = taskRepository.save(task);

        ImageQueueV3Request imageQueueRequest = ImageTask.toImageQueueRequest(saved.getId(), request, checkpoint.getModelName(), newPrompt, checkpoint.getNegativePrompt(), member);
        messageProducer.sendPlainCreationMessage(imageQueueRequest);
    }

    @Override
    public void processImageCreationFaceDetailer(ImageQueueRequest message) { //비동기 에러나면 어칼껀데

        redisService.pushToQueue("image", message.getTaskId());

        network.createImageFaceDetailer(
                message.getTaskId(),
                message.getCheckpoint(),
                message.getLora(),
                message.getPrompt(),
                message.getNegativePrompt(),
                message.getWidth(),
                message.getHeight(),
                webhookUrl + "/api/images/webhook"
        );
    }

    @Override
    public void processImageCreationTransactional(ImageQueueRequest message) { //비동기 에러나면 어칼껀데

        redisService.pushToQueue("image", message.getTaskId());

        network.createImage(
                message.getTaskId(),
                message.getCheckpoint(),
                message.getLora(),
                message.getPrompt(),
                message.getNegativePrompt(),
                message.getWidth(),
                message.getHeight(),
                webhookUrl + "/api/images/webhook"
        );
    }

    @Override
    public void processPlainCreationTransactional(ImageQueueV3Request message) { //비동기 에러나면 어칼껀데

        redisService.pushToQueue("image", message.getTaskId());

        network.createImagePlain(
                message.getTaskId(),
                message.getCheckpoint(),
                message.getPrompt(),
                message.getNegativePrompt(),
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
