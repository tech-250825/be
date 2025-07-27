package com.ll.demo03.lora.controller.response;

import com.ll.demo03.lora.domain.Lora;
import com.ll.demo03.lora.domain.MediaType;
import com.ll.demo03.lora.infrasturcture.LoraEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class LoraResponse {
    private Long id;
    private String name;
    private String modelName;
    private String image;

    public static LoraResponse from(Lora lora){
        return LoraResponse.builder()
                .id(lora.getId())
                .name(lora.getName())
                .modelName(lora.getModelName())
                .image(lora.getImage())
                .build();
    }

}

