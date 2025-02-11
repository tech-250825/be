package com.ll.demo03.domain.adminImage.entity;

import com.ll.demo03.domain.imageCategory.entity.ImageCategory;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
public class AdminImage extends BaseEntity {

    private String url;

    private String prompt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ImageCategory category;
}
