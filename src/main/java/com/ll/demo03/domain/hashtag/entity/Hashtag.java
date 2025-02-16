package com.ll.demo03.domain.hashtag.entity;

import com.ll.demo03.domain.adminImage.entity.AdminImage;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.ll.demo03.global.base.BaseEntity;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "hashtags")
public class Hashtag extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "admin_image_id", nullable = false)
    private AdminImage adminImage;

    public static Hashtag create(String name, AdminImage adminImage) {
        return Hashtag.builder()
                .name(name)
                .adminImage(adminImage)
                .build();
    }
}