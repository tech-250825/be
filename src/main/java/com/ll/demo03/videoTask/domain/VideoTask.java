package com.ll.demo03.videoTask.domain;

import com.ll.demo03.global.domain.Status;
import com.ll.demo03.global.port.Network;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.controller.request.VideoQueueRequest;
import com.ll.demo03.videoTask.controller.request.VideoTaskRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class VideoTask {

    private final Long id;
    private final String prompt;
    private final String lora;
    private final String runpodId;
    private final Status status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final Member creator;

    private final int width;
    private final int height;
    private final int numFrames;

    @Builder
    public VideoTask(Long id, String prompt, String lora, String runpodId, Status status,
                     LocalDateTime createdAt, LocalDateTime modifiedAt, Member creator,
                     int width, int height, int numFrames) {
        this.id = id;
        this.prompt = prompt;
        this.lora = lora;
        this.runpodId = runpodId;
        this.status = status;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.creator = creator;
        this.width = width;
        this.height = height;
        this.numFrames = numFrames;
    }

    public static VideoTask from(Member creator, VideoQueueRequest queueRequest) {
        return VideoTask.builder()
                .prompt(queueRequest.getPrompt())
                .lora(queueRequest.getLora())
                .width(queueRequest.getWidth())
                .height(queueRequest.getHeight())
                .numFrames(queueRequest.getNumFrames())
                .creator(creator)
                .build();
    }

    public static VideoTaskRequest updatePrompt(VideoTaskRequest request, Network network) {
        String newPrompt = network.modifyPrompt(request.getLora(), request.getPrompt());
        String finalPrompt = (newPrompt == null || newPrompt.isBlank()) ? request.getPrompt() : newPrompt;
        return new VideoTaskRequest(request.getLora(), finalPrompt, request.getWidth(), request.getHeight(), request.getNumFrames());
    }

    public VideoTask updateStatus(Status status, String runpodId) {
        return VideoTask.builder()
                .id(id)
                .prompt(prompt)
                .lora(lora)
                .runpodId(runpodId)
                .status(status)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .creator(creator)
                .width(width)
                .height(height)
                .numFrames(numFrames)
                .build();
    }

    public static VideoQueueRequest toQueueRequest(VideoTaskRequest request, Member creator) {
        return new VideoQueueRequest(
                request.getLora(),
                request.getPrompt(),
                request.getWidth(),
                request.getHeight(),
                request.getNumFrames(),
                creator.getId()
        );
    }
}
