package com.ll.demo03.domain.like.entity;

import com.ll.demo03.domain.image.entity.Image;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "user_like")
public class Like extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") //FK 컬럼명
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id") //FK 컬럼명
    private Image image;

}
