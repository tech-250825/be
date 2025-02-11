package com.ll.demo03.domain.imageCategory.entity;

import com.ll.demo03.domain.adminImage.entity.AdminImage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class ImageCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ImageCategory parent;

    @OneToMany(mappedBy = "parent")
    private List<ImageCategory> subCategories;

    @OneToMany(mappedBy = "category")
    private List<AdminImage> images;
}
