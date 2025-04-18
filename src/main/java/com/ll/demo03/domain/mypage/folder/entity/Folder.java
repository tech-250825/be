package com.ll.demo03.domain.mypage.folder.entity;

import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Folder extends BaseEntity {
    private String name;

    @OneToMany(mappedBy = "folder", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<Image> images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void updateName(String name) {
        this.name=name;
    }
}
