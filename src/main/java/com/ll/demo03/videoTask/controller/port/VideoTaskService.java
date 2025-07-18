package com.ll.demo03.videoTask.controller.port;

import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.imageTask.controller.response.TaskOrImageResponse;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.controller.request.VideoTaskRequest;
import com.ll.demo03.videoTask.controller.response.TaskOrVideoResponse;
import com.ll.demo03.videoTask.domain.VideoTaskInitiate;

import java.util.List;

public interface VideoTaskService {
    void initate(VideoTaskRequest request, Member member);
    void process(VideoTaskInitiate message);
    PageResponse<List<TaskOrVideoResponse>> getMyTasks(Member member, CursorBasedPageable pageable);
}
