package com.ll.demo03.member.controller.response;

import com.ll.demo03.member.domain.Member;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {
    private long id;
    private String profileImage;
    private int credit;

    public static MemberDto of(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .profileImage(member.getProfile())
                .credit(member.getCredit())
                .build();
    }

}