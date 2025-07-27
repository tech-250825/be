package com.ll.demo03.lora.infrasturcture;

import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.lora.domain.Lora;
import com.ll.demo03.lora.domain.MediaType;
import com.ll.demo03.lora.domain.StyleType;
import com.ll.demo03.member.infrastructure.MemberEntity;
import com.ll.demo03.videoTask.domain.VideoTask;
import com.ll.demo03.videoTask.infrastructure.VideoTaskEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "loras")
public class LoraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 기본 키 필요

    private String name;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @Enumerated(EnumType.STRING)
    private StyleType styleType;

    private String image;

    private String modelName;

    private String triggerWord;

    private String prompt;

    public Lora toModel() {
        return Lora.builder()
                .id(id)
                .name(name)
                .mediaType(mediaType)
                .styleType(styleType)
                .image(image)
                .modelName(modelName)
                .build();
    }

    public static LoraEntity from(Lora lora) {
        LoraEntity loraEntity = new LoraEntity();
        loraEntity.id = lora.getId();
        loraEntity.name = lora.getName();
        loraEntity.mediaType = lora.getMediaType();
        loraEntity.styleType = lora.getStyleType();
        loraEntity.image = lora.getImage();
        loraEntity.modelName = lora.getModelName();
        loraEntity.triggerWord = lora.getTriggerWord();
        loraEntity.prompt = lora.getPrompt();
        return loraEntity;
    }
}
