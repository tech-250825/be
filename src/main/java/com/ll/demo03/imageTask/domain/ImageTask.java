package com.ll.demo03.imageTask.domain;

import com.ll.demo03.global.domain.Status;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import com.ll.demo03.global.port.Network;
import com.ll.demo03.imageTask.controller.request.ImageTaskRequest;
import com.ll.demo03.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ImageTask {

    private final Long id;
    private final String prompt;
    private final String lora;
    private final String runpodId;
    private final Status status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final Member creator;

    @Builder
    public ImageTask(Long id, String prompt, String lora, String runpodId, Status status, LocalDateTime createdAt, LocalDateTime modifiedAt, Member creator) {
        this.id = id;
        this.prompt = prompt;
        this.lora = lora;
        this.runpodId = runpodId;
        this.status = status;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.creator = creator;
    }

    public static ImageTask from(Member creator, ImageQueueRequest queueRequest) {
        return ImageTask.builder()
                .prompt(queueRequest.getPrompt())
                .lora(queueRequest.getLora())
                .creator(creator)
                .build();
    }

    public ImageTask updateStatus(Status status, String runpodId){
        return ImageTask.builder()
                .id(id)
                .prompt(prompt)
                .lora(lora)
                .runpodId(runpodId)
                .status(status)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .creator(creator)
                .build();
    }

    public static ImageTaskRequest updatePrompt(ImageTaskRequest request, Network network) {
        String newPrompt = network.modifyPrompt(request.getLora() , request.getPrompt());
        String finalPrompt = (newPrompt == null || newPrompt.isBlank()) ? request.getPrompt() : newPrompt;
        return new ImageTaskRequest(request.getLora(), finalPrompt);
    }

    public static ImageQueueRequest toImageQueueRequest(ImageTaskRequest imageTaskRequest, Member creator) {
        return new ImageQueueRequest(imageTaskRequest.getLora(), imageTaskRequest.getPrompt(), creator.getId());
    }
}
