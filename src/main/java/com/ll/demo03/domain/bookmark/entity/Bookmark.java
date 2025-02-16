package com.ll.demo03.domain.bookmark.entity;

import com.ll.demo03.domain.adminImage.entity.AdminImage;
import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Bookmark extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") //FK 컬럼명
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adminImage_id") //FK 컬럼명
    private AdminImage image;

}
