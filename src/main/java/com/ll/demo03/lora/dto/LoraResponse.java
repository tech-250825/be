package com.ll.demo03.lora.dto;

import com.ll.demo03.lora.entity.Lora;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoraResponse {
    private String name;
    private String mediaType;
    private String styleType;
    private String modelName;
    private String image;

    public LoraResponse(String name) {
        this.name = name;
    }

    public static LoraResponse from(Lora lora) {
        LoraResponse response = new LoraResponse(lora.getName());
        response.setMediaType(String.valueOf(lora.getMediaType()));
        response.setStyleType(String.valueOf(lora.getStyleType()));
        response.setModelName(lora.getModelName());
        response.setImage(lora.getImage());
        return response;
    }

}

