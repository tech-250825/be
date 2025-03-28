package com.ll.demo03.domain.upscaledTask.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ll.demo03.domain.task.dto.WebhookData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpscaleWebhookEvent {
    private long timestamp;
    private UpscaleWebhookData data;
}
