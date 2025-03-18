package com.ll.demo03.domain.member.dto;

import com.ll.demo03.domain.member.entity.Member;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {
    private String nickname;
    private int credit;

    public static MemberDto of(Member member) {
        return MemberDto.builder()
                .nickname(member.getNickname())
                .credit(member.getCredit())
                .build();
    }
}