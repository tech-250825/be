package com.ll.demo03.domain.member.dto;

import com.ll.demo03.domain.member.entity.Member;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {
    private String email;
    private String name;
    private String profile;
    private int credit;

    public static MemberDto of(Member member) {
        return MemberDto.builder()
                .email(member.getEmail())
                .name(member.getName())
                .profile(member.getProfile())
                .credit(member.getCredit())
                .build();
    }
}