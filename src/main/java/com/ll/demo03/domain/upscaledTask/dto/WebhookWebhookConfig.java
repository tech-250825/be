package com.ll.demo03.domain.upscaledTask.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebhookWebhookConfig {
    private String endpoint;
    private String secret;
}
