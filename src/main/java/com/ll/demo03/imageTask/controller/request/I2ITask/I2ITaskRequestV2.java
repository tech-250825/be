package com.ll.demo03.imageTask.controller.request.I2ITask;

import com.ll.demo03.global.domain.ResolutionProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class I2ITaskRequestV2 {
    private String prompt;
    private ResolutionProfile resolutionProfile;
}
