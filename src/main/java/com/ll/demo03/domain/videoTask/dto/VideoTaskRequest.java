package com.ll.demo03.domain.videoTask.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoTaskRequest {
    private String prompt;
    private String imageUrl;

}
