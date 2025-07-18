package com.ll.demo03.videoTask.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoTaskRequest {
    private String lora;
    private String prompt;
}
