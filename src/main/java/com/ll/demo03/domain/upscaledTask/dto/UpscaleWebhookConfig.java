package com.ll.demo03.domain.upscaledTask.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpscaleWebhookConfig {
    private String service_mode;
    private WebhookWebhookConfig webhook_config;
}

