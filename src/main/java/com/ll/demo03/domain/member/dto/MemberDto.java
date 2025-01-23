package com.ll.demo03.domain.member.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class MemberDto {
    private String email;
    private String name;
    private String profile;
    private int token;
}