package com.ll.demo03.imageTask.domain;

import com.ll.demo03.global.domain.Status;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ImageTaskUpdate {

    private final Status status;

    @Builder
    public ImageTaskUpdate(Status status) {
        this.status = status;
    }
}

