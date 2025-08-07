package com.ll.demo03.mock;

import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.global.infrastructure.MessageProducerImpl;
import com.ll.demo03.global.port.CursorPaginationService;
import com.ll.demo03.imageTask.controller.port.ImageTaskService;
import com.ll.demo03.imageTask.service.ImageTaskPaginationStrategy;
import com.ll.demo03.imageTask.service.ImageTaskResponseConverter;
import com.ll.demo03.imageTask.service.ImageTaskServiceImpl;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.member.service.port.MemberRepository;
import com.ll.demo03.weight.service.port.WeightRepository;
import com.ll.demo03.weight.controller.port.WeightService;
import lombok.Builder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

public class TestContainer {

    public final ImageTaskRepository imageTaskRepository;
    public final ImageTaskPaginationStrategy imageTaskPaginationStrategy;
    public final MemberRepository memberRepository;
    public final UGCRepository ugcRepository;
    public final ImageTaskResponseConverter imageTaskResponseConverter;
    public final ImageTaskService imageTaskService;
    public final FakeMessageProducer fakeMessageProducer;
    public final FakeRedisService fakeRedisService;
    public final FakeNetwork fakeNetwork;
    public final FakeWeightRepository fakeWeightRepository;
    public final FakeWeightService fakeWeightService;

    @Builder
    public TestContainer(){
        this.memberRepository = new FakeMemberRepository();
        this.fakeMessageProducer = new FakeMessageProducer();
        this.imageTaskRepository = new FakeImageTaskRepository();
        this.ugcRepository = new FakeUGCRepository();
        this.imageTaskPaginationStrategy = new ImageTaskPaginationStrategy(this.imageTaskRepository);
        this.imageTaskResponseConverter = new ImageTaskResponseConverter(this.ugcRepository);
        this.fakeRedisService = new FakeRedisService();
        this.fakeNetwork = new FakeNetwork();
        this.fakeWeightRepository = new FakeWeightRepository();
        this.fakeWeightService = new FakeWeightService();
        this.imageTaskService = new ImageTaskServiceImpl(
                imageTaskRepository,
                memberRepository,
                fakeRedisService,
                fakeNetwork,
                fakeMessageProducer,
                new CursorPaginationService(),
                imageTaskPaginationStrategy,
                imageTaskResponseConverter,
                fakeWeightRepository,
                fakeWeightService,
                ugcRepository
        );

        ReflectionTestUtils.setField(imageTaskService, "webhookUrl", "/fake-url");


    }

}
