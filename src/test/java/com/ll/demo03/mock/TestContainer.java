package com.ll.demo03.mock;

import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.global.port.CursorPaginationService;
import com.ll.demo03.imageTask.controller.port.ImageTaskService;
import com.ll.demo03.imageTask.service.ImageTaskPaginationStrategy;
import com.ll.demo03.imageTask.service.ImageTaskResponseConverter;
import com.ll.demo03.imageTask.service.ImageTaskServiceImpl;
import com.ll.demo03.imageTask.service.port.ImageTaskRepository;
import com.ll.demo03.member.service.port.MemberRepository;
import lombok.Builder;

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
        this.imageTaskService = ImageTaskServiceImpl.builder()
                .taskRepository(imageTaskRepository)
                .memberRepository(memberRepository)
                .redisService(fakeRedisService)
                .network(fakeNetwork)
                .messageProducer(fakeMessageProducer)
                .paginationService(new CursorPaginationService())
                .paginationStrategy(imageTaskPaginationStrategy)
                .responseConverter(imageTaskResponseConverter)
                .webhookUrl("/fake-url")
                .build();

    }

}
