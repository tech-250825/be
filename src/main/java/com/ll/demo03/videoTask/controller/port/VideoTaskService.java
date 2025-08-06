package com.ll.demo03.videoTask.controller.port;

import com.ll.demo03.global.util.CursorBasedPageable;
import com.ll.demo03.global.util.PageResponse;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.controller.request.I2VQueueRequest;
import com.ll.demo03.videoTask.controller.request.I2VTaskRequest;
import com.ll.demo03.videoTask.controller.request.T2VQueueRequest;
import com.ll.demo03.videoTask.controller.request.VideoTaskRequest;
import com.ll.demo03.videoTask.controller.response.TaskOrVideoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoTaskService {
    void initateT2V(VideoTaskRequest request, Member member);
    void initateT2V(VideoTaskRequest request, Member member, Long boardId);
    void initateI2V(VideoTaskRequest request, Member member, MultipartFile image);
    void initateI2V(I2VTaskRequest request, Member member);
    void initateI2V(I2VTaskRequest request, Member member, Long boardId);
    void initateI2V(VideoTaskRequest request, Member member, MultipartFile image, Long boardId);
    void initateI2VFromLatestFrame(VideoTaskRequest request, Member member, String videoUrl, Long boardId);
    void process(T2VQueueRequest message);
    void process(I2VQueueRequest message);
    PageResponse<List<TaskOrVideoResponse>> getMyTasks(Member member, CursorBasedPageable pageable);
    PageResponse<List<TaskOrVideoResponse>> getVideoTasksByBoardId(Long boardId, CursorBasedPageable pageable);
    void delete(Long id);
}
