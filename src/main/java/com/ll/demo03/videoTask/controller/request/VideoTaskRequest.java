package com.ll.demo03.videoTask.controller.request;

import com.ll.demo03.global.domain.ResolutionProfile;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoTaskRequest {
    @NotNull(message = "LoRA ID는 필수입니다.")
    private Long loraId;
    
    @NotBlank(message = "프롬프트는 공백일 수 없습니다.")
    private String prompt;
    
    @NotNull(message = "해상도 프로필은 필수입니다.")
    private ResolutionProfile resolutionProfile;
    
    @Min(value = 1, message = "프레임 수는 최소 1개 이상이어야 합니다.")
    @Max(value = 200, message = "프레임 수는 최대 200개 이하여야 합니다.")
    private int numFrames;
}
