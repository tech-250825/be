package com.ll.demo03.videoTask.service;


import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.board.domain.Board;
import com.ll.demo03.board.service.port.BoardRepository;
import com.ll.demo03.global.domain.Status;
import com.ll.demo03.global.port.VideoProcessingService;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.global.port.*;
import com.ll.demo03.weight.controller.port.WeightService;
import com.ll.demo03.weight.domain.Weight;
import com.ll.demo03.weight.service.port.WeightRepository;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
import com.ll.demo03.videoTask.controller.port.VideoTaskService;
import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.videoTask.controller.request.I2VQueueRequest;
import com.ll.demo03.videoTask.controller.request.I2VTaskRequest;
import com.ll.demo03.videoTask.controller.request.T2VQueueRequest;
import com.ll.demo03.videoTask.controller.request.VideoTaskRequest;
import com.ll.demo03.videoTask.controller.response.TaskOrVideoResponse;
import com.ll.demo03.videoTask.domain.VideoTask;
import com.ll.demo03.videoTask.service.port.VideoTaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final BoardVideoTaskPaginationStrategy boardPaginationStrategy;
    private final VideoTaskResponseConverter responseConverter;
    private final WeightRepository weightRepository;
    private final S3Service s3Service;
    private final WeightService weightService;
    private final UGCRepository ugcRepository;
    private final BoardRepository boardRepository;
    private final VideoProcessingService videoProcessingService;

    @Value("${custom.webhook-url}")
    private String webhookUrl;

    @Override
    public void initateT2V(VideoTaskRequest request, Member member){
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost() * (int) Math.ceil(request.getNumFrames() / 40.0);
        creator.decreaseCredit(creditCost);

        Weight lora = weightRepository.findById(request.getLoraId()).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        VideoTask task = VideoTask.from(member, lora, request);
        task = task.updateStatus(Status.IN_PROGRESS, null);
        VideoTask saved = videoTaskRepository.save(task);

        String newPrompt = weightService.addTriggerWord(lora.getId(), request.getPrompt());

        T2VQueueRequest queueRequest = VideoTask.toT2VQueueRequest(saved.getId(), request, lora.getModelName(), newPrompt, creator);
        videoMessageProducer.sendCreationMessage(queueRequest);
        memberRepository.save(creator);
    }

    @Override
    public void initateI2V(VideoTaskRequest request, Member member, MultipartFile image) {
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost() * (int) Math.ceil(request.getNumFrames() / 40.0);
        creator.decreaseCredit(creditCost);

        String url = s3Service.uploadFile(image);

        VideoTask task = VideoTask.from(member, url, request);
        task = task.updateStatus(Status.IN_PROGRESS, null);
        VideoTask saved = videoTaskRepository.save(task);

        String newPrompt = request.getPrompt();

        I2VQueueRequest queueRequest = VideoTask.toI2VQueueRequest(saved.getId(), request, url, newPrompt, creator);
        videoMessageProducer.sendCreationMessage(queueRequest);
        memberRepository.save(creator);
    }

    @Override
    public void initateI2V(I2VTaskRequest request, Member member) {
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost() * (int) Math.ceil(request.getNumFrames() / 40.0);
        creator.decreaseCredit(creditCost);

        VideoTask task = VideoTask.from(member, request);
        task = task.updateStatus(Status.IN_PROGRESS, null);
        VideoTask saved = videoTaskRepository.save(task);

        String newPrompt = request.getPrompt();

        I2VQueueRequest queueRequest = VideoTask.toI2VQueueRequest(saved.getId(), request, newPrompt, creator);
        videoMessageProducer.sendCreationMessage(queueRequest);
        memberRepository.save(creator);
    }

    @Override
    public void initateI2V(I2VTaskRequest request, Member member, Long boardId) {
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost() * (int) Math.ceil(request.getNumFrames() / 40.0);
        creator.decreaseCredit(creditCost);

        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        VideoTask task = VideoTask.from(member, request, board);
        task = task.updateStatus(Status.IN_PROGRESS, null);
        VideoTask saved = videoTaskRepository.save(task);

        String newPrompt = request.getPrompt();

        I2VQueueRequest queueRequest = VideoTask.toI2VQueueRequest(saved.getId(), request, newPrompt, creator);
        videoMessageProducer.sendCreationMessage(queueRequest);
        memberRepository.save(creator);
    }

    @Override
    public void initateT2V(VideoTaskRequest request, Member member, Long boardId) {
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost() * (int) Math.ceil(request.getNumFrames() / 40.0);
        creator.decreaseCredit(creditCost);

        Weight lora = weightRepository.findById(request.getLoraId()).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        VideoTask task = VideoTask.from(member, lora, request, board);
        task = task.updateStatus(Status.IN_PROGRESS, null);
        VideoTask saved = videoTaskRepository.save(task);

        String newPrompt = weightService.addTriggerWord(lora.getId(), request.getPrompt());

        T2VQueueRequest queueRequest = VideoTask.toT2VQueueRequest(saved.getId(), request, lora.getModelName(), newPrompt, creator);
        videoMessageProducer.sendCreationMessage(queueRequest);
        memberRepository.save(creator);
    }

    @Override
    public void initateI2V(VideoTaskRequest request, Member member, MultipartFile image, Long boardId) {
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost() * (int) Math.ceil(request.getNumFrames() / 40.0);
        creator.decreaseCredit(creditCost);

        String url = s3Service.uploadFile(image);

        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        VideoTask task = VideoTask.from(member, url, request, board);
        task = task.updateStatus(Status.IN_PROGRESS, null);
        VideoTask saved = videoTaskRepository.save(task);

        String newPrompt = request.getPrompt();

        I2VQueueRequest queueRequest = VideoTask.toI2VQueueRequest(saved.getId(), request, url, newPrompt, creator);
        videoMessageProducer.sendCreationMessage(queueRequest);
        memberRepository.save(creator);
    }

    @Override
    public void initateI2VFromLatestFrame(I2VTaskRequest request, Member member, Long boardId) {
        Member creator = memberRepository.findById(member.getId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        int creditCost = request.getResolutionProfile().getBaseCreditCost() * (int) Math.ceil(request.getNumFrames() / 40.0);
        creator.decreaseCredit(creditCost);

        MultipartFile latestFrame = videoProcessingService.extractLatestFrameFromVideo(request.getImageUrl());
        String url = s3Service.uploadFile(latestFrame);

        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        VideoTask task = VideoTask.from(member, request, board);
        task = task.updateStatus(Status.IN_PROGRESS, null);
        VideoTask saved = videoTaskRepository.save(task);

        String newPrompt = request.getPrompt();

        I2VQueueRequest queueRequest = VideoTask.toI2VQueueRequest(saved.getId(), request, url, newPrompt, creator);
        videoMessageProducer.sendCreationMessage(queueRequest);
        memberRepository.save(creator);
    }

    @Override
    public void process(T2VQueueRequest message) { //이건 비동기므로 에러가 나도 /api/videos/create에서 에러 반환 못받는다ㅜㅜ 해결책 고안할 것 .
        Long taskId = message.getTaskId();

        redisService.pushToQueue("t2v",taskId);

        network.createT2VVideo(
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
    public void process(I2VQueueRequest message) {
        Long taskId = message.getTaskId();

        redisService.pushToQueue("i2v", taskId);

        network.createI2VVideo(
                taskId,
                message.getPrompt(),
                message.getUrl(),
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

    @Override
    public PageResponse<List<TaskOrVideoResponse>> getVideoTasksByBoardId(Long boardId, CursorBasedPageable pageable) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        boardPaginationStrategy.setBoard(board);

        return paginationService.getPagedContent(board.getMember(), pageable, boardPaginationStrategy, responseConverter);
    }

    @Override
    public void delete(Long id){
        VideoTask task = videoTaskRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        List<UGC> ugcs = ugcRepository.findAllByVideoTaskId(task.getId());
        ugcRepository.deleteAll(ugcs);

        videoTaskRepository.delete(task);
    }

}