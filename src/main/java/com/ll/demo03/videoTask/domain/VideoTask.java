package com.ll.demo03.videoTask.domain;

import com.ll.demo03.board.domain.Board;
import com.ll.demo03.global.domain.Status;
import com.ll.demo03.global.domain.ResolutionProfile;
import com.ll.demo03.weight.domain.Weight;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.controller.request.I2VQueueRequest;
import com.ll.demo03.videoTask.controller.request.I2VTaskRequest;
import com.ll.demo03.videoTask.controller.request.T2VQueueRequest;
import com.ll.demo03.videoTask.controller.request.VideoTaskRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class VideoTask {

    private final Long id;
    private final String prompt;
    private final Weight lora;
    private final String imageUrl;
    private final String runpodId;
    private final Status status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final Member creator;
    private final ResolutionProfile resolutionProfile;
    private final int numFrames;
    private final Board board;

    @Builder
    public VideoTask(Long id, String prompt, Weight lora, String imageUrl, String runpodId, Status status,
                     LocalDateTime createdAt, LocalDateTime modifiedAt, Member creator,
                     ResolutionProfile resolutionProfile, int numFrames, Board board) {
        this.id = id;
        this.prompt = prompt;
        this.lora = lora;
        this.imageUrl = imageUrl;
        this.runpodId = runpodId;
        this.status = status;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.creator = creator;
        this.resolutionProfile = resolutionProfile;
        this.numFrames = numFrames;
        this.board = board;
    }

    public static VideoTask from(Member creator, Weight lora, VideoTaskRequest request) {
        return VideoTask.builder()
                .prompt(request.getPrompt())
                .lora(lora)
                .resolutionProfile(request.getResolutionProfile())
                .numFrames(request.getNumFrames())
                .creator(creator)
                .build();
    }

    public static VideoTask from(Member creator,  Weight lora, VideoTaskRequest request, Board board) {
        return VideoTask.builder()
                .prompt(request.getPrompt())
                .lora(lora)
                .resolutionProfile(request.getResolutionProfile())
                .numFrames(request.getNumFrames())
                .creator(creator)
                .board(board)
                .build();
    }

    public static VideoTask from(Member creator, I2VTaskRequest request) {
        return VideoTask.builder()
                .prompt(request.getPrompt())
                .imageUrl(request.getImageUrl())
                .resolutionProfile(request.getResolutionProfile())
                .numFrames(request.getNumFrames())
                .creator(creator)
                .build();
    }

    public static VideoTask from(Member creator, I2VTaskRequest request, Board board) {
        return VideoTask.builder()
                .prompt(request.getPrompt())
                .imageUrl(request.getImageUrl())
                .resolutionProfile(request.getResolutionProfile())
                .numFrames(request.getNumFrames())
                .creator(creator)
                .board(board)
                .build();
    }

    public static VideoTask from(Member creator, String url, VideoTaskRequest request) {
        return VideoTask.builder()
                .prompt(request.getPrompt())
                .imageUrl(url)
                .resolutionProfile(request.getResolutionProfile())
                .numFrames(request.getNumFrames())
                .creator(creator)
                .build();
    }

    public static VideoTask from(Member creator, String url, VideoTaskRequest request, Board board) {
        return VideoTask.builder()
                .prompt(request.getPrompt())
                .imageUrl(url)
                .resolutionProfile(request.getResolutionProfile())
                .numFrames(request.getNumFrames())
                .creator(creator)
                .board(board)
                .build();
    }

    public VideoTask updateStatus(Status status, String runpodId) {
        return VideoTask.builder()
                .id(id)
                .prompt(prompt)
                .lora(lora)
                .imageUrl(imageUrl)
                .runpodId(runpodId)
                .status(status)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .creator(creator)
                .resolutionProfile(resolutionProfile)
                .numFrames(numFrames)
                .board(board)
                .build();
    }

    public static T2VQueueRequest toT2VQueueRequest(Long taskId, VideoTaskRequest request, String modelName, String newPrompt, Member creator) {
        return new T2VQueueRequest(
                taskId,
                modelName,
                newPrompt,
                request.getResolutionProfile().getWidth(),
                request.getResolutionProfile().getHeight(),
                request.getNumFrames(),
                creator.getId()
        );
    }

    public static I2VQueueRequest toI2VQueueRequest(Long taskId, VideoTaskRequest request, String url, String newPrompt, Member creator) {
        return new I2VQueueRequest(
                taskId,
                newPrompt,
                url,
                request.getResolutionProfile().getWidth(),
                request.getResolutionProfile().getHeight(),
                request.getNumFrames(),
                creator.getId()
        );
    }

    public static I2VQueueRequest toI2VQueueRequest(Long taskId, I2VTaskRequest request, String newPrompt, Member creator) {
        return new I2VQueueRequest(
                taskId,
                newPrompt,
                request.getImageUrl(),
                request.getResolutionProfile().getWidth(),
                request.getResolutionProfile().getHeight(),
                request.getNumFrames(),
                creator.getId()
        );
    }
}
