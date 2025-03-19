package com.ll.demo03.domain.sharedImage.entity;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SharedImage extends BaseEntity {

    private int likeCount;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;
}
