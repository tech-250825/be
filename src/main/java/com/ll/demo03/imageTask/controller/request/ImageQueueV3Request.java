package com.ll.demo03.imageTask.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageQueueV3Request {
    private Long taskId;
    private String checkpoint;
    private String prompt;
    private String negativePrompt;
    private int width;
    private int height;
    private Long memberId;
}
