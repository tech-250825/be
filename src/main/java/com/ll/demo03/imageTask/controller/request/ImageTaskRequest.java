package com.ll.demo03.imageTask.controller.request;

import com.ll.demo03.global.domain.ResolutionProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageTaskRequest {
    private Long loraId;
    private String prompt;
    private ResolutionProfile resolutionProfile;
}
