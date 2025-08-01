package com.ll.demo03.videoTask.controller.request;

import com.ll.demo03.global.domain.ResolutionProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoTaskRequest {
    private Long loraId;
    private String prompt;
    private ResolutionProfile resolutionProfile;
    private int numFrames;
}
