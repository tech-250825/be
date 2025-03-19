package com.ll.demo03.domain.member.service;

import com.ll.demo03.domain.member.dto.MemberDto;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.member.repository.MemberRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberDto findMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. 이메일: " + email));

        MemberDto memberDto = MemberDto.of(member);
        return memberDto;
    }

    public Long findIdByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. 이메일: " + email));

        return member.getId();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyCredit() {
        memberRepository.resetAllMembersCredit();
    }

    @Transactional
    public MemberDto updateNickname(Long memberId, String nickname) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCESS_DENIED));

        if (memberRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.DUPLICATED_NICKNAME);
        }

        member.updateNickname(nickname);
        Member savedMember = memberRepository.save(member);

        return MemberDto.of(savedMember);
    }
}
