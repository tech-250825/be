package com.ll.demo03.domain.task.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WebhookConfig {
    private String service_mode;
    private WebhookConfigDetail webhook_config;
}
