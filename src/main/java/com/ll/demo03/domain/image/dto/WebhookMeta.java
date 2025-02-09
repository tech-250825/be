package com.ll.demo03.domain.image.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WebhookMeta {
    private String created_at;
    private String started_at;
    private String ended_at;
    private WebhookUsage usage;
    private boolean is_using_private_pool;
    private String model_version;
    private String process_mode;
    private boolean failover_triggered;
}
