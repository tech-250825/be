package com.ll.demo03.domain.task.dto;

import lombok.Getter;

@Getter
public class ImageRequest {
    private String prompt;
    private String ratio;
    private String crefUrl;
    private String srefUrl;
}
