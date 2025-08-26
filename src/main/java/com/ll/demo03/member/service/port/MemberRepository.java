package com.ll.demo03.member.service.port;

import com.ll.demo03.member.domain.Member;

import java.util.Optional;

public interface MemberRepository {

    Optional<Member> findById(Long id);

    void delete(Member member);

    Optional<Member> findByEmail(String email);

    Member save(Member newMember);
}
