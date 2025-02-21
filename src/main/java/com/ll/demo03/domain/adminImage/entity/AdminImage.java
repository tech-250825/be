package com.ll.demo03.domain.adminImage.entity;

import com.ll.demo03.domain.hashtag.entity.Hashtag;
import com.ll.demo03.domain.imageCategory.entity.ImageCategory;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AdminImage extends BaseEntity {

    private String url;

    private String prompt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ImageCategory category;

    @OneToMany(mappedBy = "adminImage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Hashtag> hashtags = new ArrayList<>();

    public void addHashtag(Hashtag hashtag) {
        hashtags.add(hashtag);
        hashtag.setAdminImage(this);
    }
}
