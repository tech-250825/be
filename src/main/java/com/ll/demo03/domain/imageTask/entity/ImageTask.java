package com.ll.demo03.domain.imageTask.entity;

import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@Entity
public class ImageTask extends BaseEntity {

    private String prompt;

    private String lora;

    private String runpodId;

    private String status;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
}