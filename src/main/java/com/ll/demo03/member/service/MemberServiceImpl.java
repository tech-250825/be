package com.ll.demo03.member.service;

import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl {

    private final MemberRepository memberRepository;
    private final UGCRepository ugcRepository;
    private final ImageTaskRepository imageTaskRepository;
    private final com.ll.demo03.videoTask.service.port.VideoTaskRepository videoTaskRepository;
    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyCredit() {
        memberRepository.resetAllMembersCredit();
    }


    @Transactional
    public void delete(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        notificationRepository.deleteByMemberId(memberId);

        videoTaskRepository.deleteByMemberId(memberId);

        imageTaskRepository.deleteByMemberId(memberId);

        ugcRepository.deleteByMemberId(memberId);

        memberRepository.delete(member);
    }
}
