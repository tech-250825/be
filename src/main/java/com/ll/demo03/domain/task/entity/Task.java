package com.ll.demo03.domain.task.entity;

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
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Task extends BaseEntity {
    private String ratio;
    private String rawPrompt;
    private String gptPrompt;
    private String taskId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;
}
