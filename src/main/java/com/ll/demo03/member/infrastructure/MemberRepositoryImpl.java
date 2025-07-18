package com.ll.demo03.member.infrastructure;

import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id).map(MemberEntity::toModel);
    }

    @Override
    public void delete(Member member) {
        memberJpaRepository.delete(MemberEntity.from(member));
    }

    @Override
    public void resetAllMembersCredit(){
        memberJpaRepository.resetAllMembersCredit();
    };
}
