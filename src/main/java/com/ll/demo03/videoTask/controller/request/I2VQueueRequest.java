package com.ll.demo03.videoTask.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class I2VQueueRequest {
    private String lora;
    private String prompt;
    private String url;
    private int width;
    private int height;
    private int numFrames;
    private Long memberId;
}