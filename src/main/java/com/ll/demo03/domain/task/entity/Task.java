package com.ll.demo03.domain.task.entity;

import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.persistence.*;
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

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String rawPrompt;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String gptPrompt;

    private String taskId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;
}
