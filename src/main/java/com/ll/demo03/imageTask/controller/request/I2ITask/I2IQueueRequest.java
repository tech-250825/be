package com.ll.demo03.imageTask.controller.request.I2ITask;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class I2IQueueRequest {
    private Long taskId;
    private String prompt;
    private String imageUrl;
    private Long memberId;
}
