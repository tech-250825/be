package com.ll.demo03.imageTask.controller.request;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageQueueRequest {
    private String lora;
    private String prompt;
    private Long memberId;
}