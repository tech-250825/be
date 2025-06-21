package com.ll.demo03.domain.videoTask.entity;

import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@Entity
public class VideoTask extends BaseEntity {

    private String prompt;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String taskId;
}