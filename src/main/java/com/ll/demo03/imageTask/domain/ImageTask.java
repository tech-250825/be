package com.ll.demo03.imageTask.domain;

import com.ll.demo03.global.domain.Status;
import com.ll.demo03.global.domain.ResolutionProfile;
import com.ll.demo03.imageTask.controller.request.I2ITask.I2IQueueRequest;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import com.ll.demo03.global.port.Network;
import com.ll.demo03.imageTask.controller.request.ImageQueueV3Request;
import com.ll.demo03.imageTask.controller.request.ImageTaskRequest;
import com.ll.demo03.imageTask.controller.request.ImageTaskV3Request;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.weight.domain.Weight;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ImageTask {

    private final Long id;
    private final String prompt;
    private final String oldPrompt;
    private final String imageUrl;
    private final Weight checkpoint;
    private final Weight lora;
    private final String runpodId;
    private final Status status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final Member creator;
    private final ResolutionProfile resolutionProfile;

    @Builder
    public ImageTask(Long id, String prompt, String oldPrompt, String imageUrl, Weight checkpoint, Weight lora, String runpodId, Status status, LocalDateTime createdAt, LocalDateTime modifiedAt, Member creator, ResolutionProfile resolutionProfile) {
        this.id = id;
        this.prompt = prompt;
        this.oldPrompt = oldPrompt;
        this.imageUrl = imageUrl;
        this.checkpoint = checkpoint;
        this.lora = lora;
        this.runpodId = runpodId;
        this.status = status;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.creator = creator;
        this.resolutionProfile = resolutionProfile;
    }

    public static ImageTask from(Member creator, Weight checkpoint, Weight lora, String oldPrompt,  String prompt,ResolutionProfile profile) {
        return ImageTask.builder()
                .prompt(prompt)
                .oldPrompt(oldPrompt)
                .checkpoint(checkpoint)
                .lora(lora)
                .resolutionProfile(profile)
                .creator(creator)
                .build();
    }

    public static ImageTask from(Member creator, Weight checkpoint, String oldPrompt, String prompt, ResolutionProfile profile) {
        return ImageTask.builder()
                .prompt(prompt)
                .oldPrompt(oldPrompt)
                .checkpoint(checkpoint)
                .resolutionProfile(profile)
                .creator(creator)
                .build();
    }

    public static ImageTask from(Member creator, String prompt, String imageUrl, ResolutionProfile profile) {
        return ImageTask.builder()
                .prompt(prompt)
                .imageUrl(imageUrl)
                .resolutionProfile(profile)
                .creator(creator)
                .build();
    }

    public ImageTask updateStatus(Status status, String runpodId){
        return ImageTask.builder()
                .id(id)
                .prompt(prompt)
                .oldPrompt(oldPrompt)
                .imageUrl(imageUrl)
                .checkpoint(checkpoint)
                .lora(lora)
                .runpodId(runpodId)
                .status(status)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .creator(creator)
                .resolutionProfile(resolutionProfile)
                .build();
    }

    public static ImageQueueRequest toImageQueueRequest(Long taskId, ImageTaskRequest imageTaskRequest, String checkpoint, String lora, String newPrompt, String negativePrompt, Member creator) {
        return new ImageQueueRequest(taskId, checkpoint, lora, newPrompt, negativePrompt, imageTaskRequest.getResolutionProfile().getWidth(), imageTaskRequest.getResolutionProfile().getHeight(), creator.getId());
    }

    public static ImageQueueV3Request toImageQueueRequest(Long taskId, ImageTaskV3Request imageTaskRequest, String checkpoint, String newPrompt, String negativePrompt, Member creator) {
        return new ImageQueueV3Request(taskId, checkpoint, newPrompt, negativePrompt, imageTaskRequest.getResolutionProfile().getWidth(), imageTaskRequest.getResolutionProfile().getHeight(), creator.getId());
    }

    public static I2IQueueRequest toI2IQueueRequest(Long taskId, String imageUrl, String newPrompt, Member creator) {
        return new I2IQueueRequest(taskId, imageUrl, newPrompt, creator.getId());
    }
}
