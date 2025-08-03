package com.ll.demo03.imageTask.controller.request;

import com.ll.demo03.global.domain.ResolutionProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageTaskRequest {
    @NotNull(message = "LoRA ID는 필수입니다.")
    private Long loraId;
    
    @NotBlank(message = "프롬프트는 공백일 수 없습니다.")
    private String prompt;
    
    @NotNull(message = "해상도 프로필은 필수입니다.")
    private ResolutionProfile resolutionProfile;
}
