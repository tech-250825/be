package com.ll.demo03.domain.image.entity;


import com.ll.demo03.domain.imageGenerate.entity.ImageGenerate;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
public class Image extends BaseEntity {
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    private ImageGenerate imageGenerate;

    public static Image of(String url, ImageGenerate imageGenerate) {
        Image image = new Image();
        image.url = url;
        image.imageGenerate = imageGenerate;
        return image;
    }
}
