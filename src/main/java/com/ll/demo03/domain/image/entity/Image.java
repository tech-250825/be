package com.ll.demo03.domain.image.entity;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Image extends BaseEntity {
    private String style; //이름
    private String object; //아이디
    private String prompt; //이메일
    private String image_url; //프로필 사진

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") //FK 컬럼명
    private Member member;

    @Enumerated(EnumType.ORDINAL)
    @Builder.Default
    private Set<UpscaledImg> upscaled_img = new HashSet<>(); //업스케일한 이미지 번호
}