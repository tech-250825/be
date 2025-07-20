package com.ll.demo03.member.domain;

import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.member.infrastructure.MemberEntity;
import lombok.*;

@Getter
public class Member  {
    private Long id;
    private String name;
    private String email;
    private String profile;
    private int credit;
    private Role role;
    private AuthProvider provider;
    private String providerId;

    @Builder
    public Member(Long id, String name, String email, String profile, int credit, Role role, AuthProvider provider, String providerId){
        this.id = id;
        this.name = name;
        this.email =email;
        this.profile = profile;
        this.credit = credit;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
    }

    public boolean canUseCredit(int amount) {
        return amount > 0 && this.credit >= amount;
    }

    public void decreaseCredit(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("감소량은 0보다 커야 합니다.");
        }
        if (!canUseCredit(amount)) {
            throw new CustomException(ErrorCode.NO_CREDIT); //에러 처리 여기서 하면 ExceptionControllerAdvice가 알아서 받는다 .
        }
        this.credit -= amount;
    }

    public void increaseCredit(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("증가량은 0보다 커야 합니다.");
        }
        this.credit += amount;
    }
}
