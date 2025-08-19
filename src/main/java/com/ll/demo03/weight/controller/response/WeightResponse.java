package com.ll.demo03.weight.controller.response;

import com.ll.demo03.weight.domain.Weight;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WeightResponse {
    private Long id;
    private String name;
    private String modelName;
    private String image;
    private boolean visible;

    public static WeightResponse from(Weight weight){
        return WeightResponse.builder()
                .id(weight.getId())
                .name(weight.getName())
                .modelName(weight.getModelName())
                .image(weight.getImage())
                .visible(weight.isVisible())
                .build();
    }

}

