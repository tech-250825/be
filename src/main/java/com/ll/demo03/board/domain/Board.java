package com.ll.demo03.board.domain;

import com.ll.demo03.member.domain.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Board {
    private final Long id;
    private final String name;
    private final Member member;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    @Builder
    public Board(Long id, String name, Member member, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.name = name;
        this.member = member;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }
}
