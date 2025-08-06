package com.ll.demo03.imageTask.controller.request;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageQueueRequest {
    private String checkpoint;
    private String lora;
    private String prompt;
    private int width;
    private int height;
    private Long memberId;
}