package com.ll.demo03.domain.adminImageCategory.entity;

import com.ll.demo03.domain.adminImage.entity.AdminImage;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.prefs.BackingStoreException;

@Entity
@Getter
@Setter
public class AdminImageCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private AdminImageCategory parent;

    @OneToMany(mappedBy = "parent")
    private List<AdminImageCategory> subCategories;

    @OneToMany(mappedBy = "category")
    private List<AdminImage> images;
}
