package com.ll.demo03.imageTask.controller.request.I2ITask;

import com.ll.demo03.global.domain.ResolutionProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class I2ITaskRequest {
    private String prompt;
    private String imageUrl;
    private ResolutionProfile resolutionProfile;
}
