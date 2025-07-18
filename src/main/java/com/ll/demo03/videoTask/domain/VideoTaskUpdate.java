package com.ll.demo03.videoTask.domain;

import com.ll.demo03.imageTask.domain.Status;
import lombok.Builder;
import lombok.Getter;

@Getter
public class VideoTaskUpdate {

    private final Status status;

    @Builder
    public VideoTaskUpdate(Status status) {
        this.status = status;
    }
}

