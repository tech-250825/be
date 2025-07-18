package com.ll.demo03.oauth.entity;

import com.ll.demo03.member.domain.AuthProvider;
import com.ll.demo03.member.infrastructure.Member;
import lombok.Builder;
import com.ll.demo03.member.domain.Role;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;

import java.util.Map;

@Builder
public record OAuth2UserInfo(
        String name,
        String email,
        String profile,
        String provider,
        String providerId
) {

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) { // registration id별로 userInfo 생성
            case "google" -> ofGoogle(attributes);
            default -> throw new CustomException(ErrorCode.ILLEGAL_REGISTRATION_ID);
        };
    }

    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .profile((String) attributes.get("picture"))
                .provider("GOOGLE")
                .providerId(attributes.get("sub").toString())
                .build();
    }

    public Member toEntity() {
        return Member.builder()
                .name(name)
                .email(email)
                .profile(profile)
                .provider(AuthProvider.valueOf(provider))
                .providerId(providerId)
                .role(Role.USER)
                .build();
    }
}
