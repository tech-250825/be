package com.ll.demo03.domain.task.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WebhookInput {
    private String aspect_ratio;
    private Integer bot_id;
    private String process_mode;
    private String prompt;
    private boolean skip_prompt_check;
}
