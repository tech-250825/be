package com.ll.demo03.domain.imageGenerate.entity;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ImageGenerate extends BaseEntity {
    private String style;
    private String ratio;
    private String prompt;
    private String taskid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") //FK 컬럼명
    private Member member;
}