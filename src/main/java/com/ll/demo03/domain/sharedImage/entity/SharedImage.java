package com.ll.demo03.domain.sharedImage.entity;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SharedImage extends BaseEntity {

    private int likeCount;

    @ManyToOne(fetch= FetchType.LAZY)
    private Image image;
}
