package com.ll.demo03.videoTask.controller.request;

import com.ll.demo03.global.domain.ResolutionProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class I2VTaskRequest {
    private Long loraId = 1L; // Default to LoRA ID 1 (Studio Ghibli style)
    private String prompt;
    private String imageUrl;
    private ResolutionProfile resolutionProfile;
    private int numFrames;
}

