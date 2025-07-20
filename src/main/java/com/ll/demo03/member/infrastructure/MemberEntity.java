package com.ll.demo03.member.infrastructure;

import com.ll.demo03.member.domain.AuthProvider;
import com.ll.demo03.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.ll.demo03.member.domain.Role;


@Getter
@Setter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberEntity {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String profile;

    private int credit = 100000;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    public static MemberEntity from(Member member){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.id = member.getId();
        memberEntity.name = member.getName();
        memberEntity.email = member.getEmail();
        memberEntity.profile = member.getProfile();
        memberEntity.credit = member.getCredit();
        memberEntity.role = member.getRole();
        memberEntity.provider = member.getProvider();
        memberEntity.providerId = member.getProviderId();
        return memberEntity;
    }

    public Member toModel(){
        return Member.builder()
                .id(this.getId())
                .name(this.getName())
                .profile(this.getProfile())
                .credit(this.getCredit())
                .role(this.getRole())
                .provider(this.getProvider())
                .providerId(this.getProviderId())
                .build();
    }
}
