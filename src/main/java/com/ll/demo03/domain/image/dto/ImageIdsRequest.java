package com.ll.demo03.domain.image.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ImageIdsRequest {
    private List<Long> imageIds;

}