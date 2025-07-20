package com.ll.demo03.videoTask.controller.port;

import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.controller.request.VideoQueueRequest;
import com.ll.demo03.videoTask.controller.request.VideoTaskRequest;
import com.ll.demo03.videoTask.controller.response.TaskOrVideoResponse;

import java.util.List;

public interface VideoTaskService {
    void initate(VideoTaskRequest request, Member member);
    void process(VideoQueueRequest message);
    PageResponse<List<TaskOrVideoResponse>> getMyTasks(Member member, CursorBasedPageable pageable);
}
