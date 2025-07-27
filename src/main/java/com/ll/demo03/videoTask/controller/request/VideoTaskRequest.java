package com.ll.demo03.videoTask.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoTaskRequest {
    private Long loraId;
    private String prompt;
    private int width;
    private int height;
    private int numFrames;
}
