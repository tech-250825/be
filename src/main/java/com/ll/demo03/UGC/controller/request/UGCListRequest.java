package com.ll.demo03.UGC.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UGCListRequest {
    private List<Long> imageIds;

}