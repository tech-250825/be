package com.ll.demo03.domain.member.dto;

import com.ll.demo03.domain.member.entity.Member;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {
    private long id;
    private String nickname;
    private String profileImage;
    private int credit;
    private long sharedImageCount;

    public static MemberDto of(Member member, long sharedImageCount) {
        return MemberDto.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .profileImage(member.getProfile())
                .credit(member.getCredit())
                .sharedImageCount(sharedImageCount)
                .build();
    }

}