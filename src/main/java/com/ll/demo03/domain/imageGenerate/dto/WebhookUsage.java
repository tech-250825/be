package com.ll.demo03.domain.imageGenerate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WebhookUsage {
    private String type;
    private Integer frozen;
    private Integer consume;
}
