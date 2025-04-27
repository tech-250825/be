package com.ll.demo03.domain.member.dto;


import com.ll.demo03.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicMemberDto {
    private long id;
    private String nickname;
    private String profileImage;

    public static PublicMemberDto of(Member member) {
        return PublicMemberDto.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .profileImage(member.getProfile())
                .build();
    }
}
