package com.ll.demo03.domain.member.service;

import com.ll.demo03.domain.member.dto.MemberDto;
import com.ll.demo03.domain.sharedImage.repository.SharedImageRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final ProfileImageService profileImageService; // 추가
    private final SharedImageRepository   sharedImageRepository;

    public MemberDto findMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다. 이메일: " + email));

        long sharedImageCount = sharedImageRepository.countByMemberId(member.getId());
        MemberDto memberDto = MemberDto.of(member, sharedImageCount);
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
    public MemberDto updateProfile(Long memberId, String nickname, MultipartFile profileImage) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCESS_DENIED));

        // 닉네임이 바뀌는 경우에만 중복 체크
        if (!member.getNickname().equals(nickname)) {
            if (memberRepository.existsByNickname(nickname)) {
                throw new CustomException(ErrorCode.DUPLICATED_NICKNAME);
            }
            member.updateNickname(nickname);
        }

        // 프로필 이미지가 있으면 업로드하고 URL 저장
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String imageUrl = profileImageService.uploadProfileImage(profileImage, memberId);
                member.updateProfile(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("프로필 이미지 업로드 중 오류가 발생했습니다.", e);
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        Member savedMember = memberRepository.save(member);
        long sharedImageCount = sharedImageRepository.countByMemberId(savedMember.getId());
        return MemberDto.of(savedMember, sharedImageCount);
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

        long sharedImageCount = sharedImageRepository.countByMemberId(savedMember.getId());
        MemberDto dto = MemberDto.of(savedMember, sharedImageCount);

        return dto;
    }
}
