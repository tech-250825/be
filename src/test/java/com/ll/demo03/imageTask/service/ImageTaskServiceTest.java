package com.ll.demo03.imageTask.service;

import com.ll.demo03.global.domain.Status;
import com.ll.demo03.global.error.ErrorCode;
import com.ll.demo03.global.exception.CustomException;
import com.ll.demo03.imageTask.controller.port.ImageTaskService;
import com.ll.demo03.imageTask.controller.request.ImageQueueRequest;
import com.ll.demo03.imageTask.controller.request.ImageTaskRequest;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
import com.ll.demo03.mock.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ImageTaskServiceTest {

    private TestContainer testContainer;
    private ImageTaskService imageTaskService;
    private MemberRepository memberRepository;
    private FakeMessageProducer messageProducer;
    private ImageTaskRepository taskRepository;
    private FakeRedisService redisService;
    private FakeNetwork network;

    @BeforeEach
    void setUp() {
        this.testContainer = TestContainer.builder().build();
        this.imageTaskService = testContainer.imageTaskService;
        this.memberRepository = testContainer.memberRepository;
        this.messageProducer = testContainer.fakeMessageProducer;
        this.taskRepository = testContainer.imageTaskRepository;
        this.redisService = testContainer.fakeRedisService;
        this.network = testContainer.fakeNetwork;

        Member member1 = Member.builder().id(1L).credit(5).build();
        Member member2 = Member.builder().id(2L).credit(0).build();
        memberRepository.save(member1);
        memberRepository.save(member2);

        // Add test Weight data
        testContainer.fakeWeightRepository.addTestWeight(1L, "test-checkpoint", "checkpoint-trigger");
        testContainer.fakeWeightRepository.addTestWeight(2L, "test-lora", "lora-trigger");
    }

    @Test
    void 프롬프트_변환하고_크레딧_차감하고_메시지_전송한다() {
        // given
        Member member = memberRepository.findById(1L).get();

        ImageTaskRequest request = ImageTaskRequest.builder()
                .prompt("고양이 사진 그려줘")
                .checkpointId(1L)
                .loraId(2L)
                .resolutionProfile(com.ll.demo03.global.domain.ResolutionProfile.RATIO_9_16_SD)
                .build();
        log.info("감소 전 크레딧: {}, 요청 감소량: {}", member.getCredit(), 1);

        // when
        imageTaskService.initate(request, member);

        // then
        Member updated = memberRepository.findById(1L).get();
        log.info("감소 전 크레딧: {}, 요청 감소량: {}", updated.getCredit(), 1);
        assertEquals(0, updated.getCredit());

        assertTrue(messageProducer.imageMessages.size() == 1);
        log.info("프롬프트:  {}" ,messageProducer.imageMessages.size());
        ImageQueueRequest sentMessage = messageProducer.imageMessages.get(0);
        log.info("프롬프트:  {}" ,sentMessage.getPrompt());
        assertTrue(sentMessage.getPrompt().contains("[FAKE_MODIFIED]"));
    }

    @Test
    void 크레딧이_0이면_에러를_발생시킨다() {
        // given
        Member member = memberRepository.findById(2L).get();

        ImageTaskRequest request = ImageTaskRequest.builder()
                .prompt("고양이 사진 그려줘")
                .checkpointId(1L)
                .loraId(2L)
                .resolutionProfile(com.ll.demo03.global.domain.ResolutionProfile.RATIO_9_16_SD)
                .build();

        // when
        //then
        assertThatThrownBy(() -> {
            imageTaskService.initate(request, member);
        }).isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.NO_CREDIT.getMessage());

    }

    @Test
    void 이미지_생성_테스크를_저장하고_큐에_넣고_네트워크_요청한다() {
        // given
        Member member = memberRepository.findById(1L).orElseThrow();
        ImageQueueRequest message = ImageQueueRequest.builder()
                .memberId(member.getId())
                .prompt("강아지 그려줘")
                .lora("cute-lora")
                .build();

        // when
        imageTaskService.processImageCreationTransactional(message);

        // then
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<ImageTask> slice = taskRepository.findByMember(member, pageRequest);
        List<ImageTask> tasks = slice.getContent();

        assertFalse(tasks.isEmpty(), "ImageTask가 저장되어야 합니다.");
        ImageTask task = tasks.get(0);
        assertEquals(Status.IN_PROGRESS, task.getStatus(), "Task 상태는 IN_PROGRESS여야 합니다.");
        assertEquals("강아지 그려줘", task.getPrompt());

        assertTrue(redisService.queueContains("image", task.getId()), "Redis 큐에 taskId가 있어야 합니다.");

        assertEquals(1, network.imageCalls.size(), "네트워크 호출이 1번 있어야 합니다.");
        FakeNetwork.ImageCall call = network.imageCalls.get(0);
        assertEquals(task.getId(), call.taskId);
        assertEquals("cute-lora", call.lora);
        assertEquals("강아지 그려줘", call.prompt);
    }

}