package com.ll.demo03.domain.upscaledTask.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ll.demo03.domain.imageTask.dto.WebhookUsage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpscaleWebhookMeta {
    private String created_at;
    private String started_at;
    private String ended_at;
    private WebhookUsage usage;
    private boolean is_using_private_pool;
    private String model_version;
    private String process_mode;
    private boolean failover_triggered;
}
