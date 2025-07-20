package com.ll.demo03.videoTask.service;

import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.global.domain.Status;
import com.ll.demo03.global.port.ResponseConverter;
import com.ll.demo03.videoTask.controller.response.TaskOrVideoResponse;
import com.ll.demo03.videoTask.domain.VideoTask;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class VideoTaskResponseConverter implements ResponseConverter<VideoTask, TaskOrVideoResponse> {
    private final UGCRepository ugcRepository;

    public VideoTaskResponseConverter(UGCRepository ugcRepository) {
        this.ugcRepository = ugcRepository;
    }

    @Override
    public List<TaskOrVideoResponse> convertToResponses(List<VideoTask> tasks) {
        List<VideoTask> completedTasks = tasks.stream()
                .filter(task -> Status.COMPLETED.equals(task.getStatus()))
                .collect(Collectors.toList());

        Map<Long, List<UGC>> taskVideoMap = Collections.emptyMap();
        if (!completedTasks.isEmpty()) {
            taskVideoMap = ugcRepository.findByVideoTaskIn(completedTasks)
                    .stream()
                    .collect(Collectors.groupingBy(ugc -> ugc.getVideoTask().getId()));
        }

        List<TaskOrVideoResponse> responses = new ArrayList<>();

        for (VideoTask task : tasks) {
            if (Status.IN_PROGRESS.equals(task.getStatus()) || Status.FAILED.equals(task.getStatus()) ) {
                responses.add(TaskOrVideoResponse.fromTask(task));
            } else if (Status.COMPLETED.equals(task.getStatus())) {
                List<UGC> videos = taskVideoMap.getOrDefault(task.getId(), Collections.emptyList());
                for (UGC video : videos) {
                    responses.add(TaskOrVideoResponse.fromVideo(task, video));
                }
            }
        }

        return responses;
    }
}

