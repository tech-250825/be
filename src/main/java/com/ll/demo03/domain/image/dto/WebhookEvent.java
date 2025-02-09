package com.ll.demo03.domain.image.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class WebhookEvent {
    private Long timestamp;
    private WebhookData data;
}

