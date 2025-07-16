package com.ll.demo03.domain.member.service;

import com.ll.demo03.domain.folder.repository.FolderRepository;
import com.ll.demo03.domain.image.repository.ImageRepository;
import com.ll.demo03.domain.like.repository.LikeRepository;
import com.ll.demo03.domain.member.dto.MemberDto;
import com.ll.demo03.domain.notification.repository.NotificationRepository;
import com.ll.demo03.domain.sharedImage.repository.SharedImageRepository;
import com.ll.demo03.domain.imageTask.repository.ImageTaskRepository;
import com.ll.demo03.domain.upscaledTask.repository.UpscaleTaskRepository;
import com.ll.demo03.domain.videoTask.repository.VideoTaskRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import jakarta.persistence.EntityNotFoundException;
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
    private final LikeRepository likeRepository;
    private final ImageRepository imageRepository;
    private final FolderRepository folderRepository;
    private final ImageTaskRepository imageTaskRepository;
    private final UpscaleTaskRepository upscaleTaskRepository;
    private final VideoTaskRepository videoTaskRepository;
    private final NotificationRepository notificationRepository;

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

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        // 1. 좋아요 삭제
        likeRepository.deleteByMemberId(memberId);

        // 2. 공유 이미지 삭제
        sharedImageRepository.deleteByMemberId(memberId);

        // 3. 알림 삭제 (member를 참조하므로 member 삭제 전에 먼저 삭제)
        notificationRepository.deleteByMemberId(memberId);

        // 4. 업스케일/비디오 태스크 삭제 (이미지를 참조할 수 있음)
        upscaleTaskRepository.deleteByMemberId(memberId);
        videoTaskRepository.deleteByMemberId(memberId);

        // 5. 태스크 삭제 (이미지를 참조할 수 있음)
        imageTaskRepository.deleteByMemberId(memberId);

        // 6. 이미지 삭제 (폴더를 참조하므로 폴더 삭제 전에 먼저 삭제)
        imageRepository.deleteByMemberId(memberId);

        // 7. 폴더 삭제 (이미지 삭제 후에 삭제)
        folderRepository.deleteByMemberId(memberId);

        // 8. 최종 멤버 삭제
        memberRepository.delete(member);
    }
}
