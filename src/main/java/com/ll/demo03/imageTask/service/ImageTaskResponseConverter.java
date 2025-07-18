package com.ll.demo03.imageTask.service;

import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.UGC.service.port.UGCRepository;
import com.ll.demo03.global.port.ResponseConverter;
import com.ll.demo03.imageTask.controller.response.TaskOrImageResponse;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.imageTask.domain.Status;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ImageTaskResponseConverter implements ResponseConverter<ImageTask, TaskOrImageResponse> {
    private final UGCRepository ugcRepository;

    public ImageTaskResponseConverter(UGCRepository ugcRepository) {
        this.ugcRepository = ugcRepository;
    }

    @Override
    public List<TaskOrImageResponse> convertToResponses(List<ImageTask> tasks) {
        List<ImageTask> completedTasks = tasks.stream()
                .filter(task -> Status.COMPLETED.equals(task.getStatus()))
                .collect(Collectors.toList());

        Map<ImageTask, List<UGC>> taskImageMap = Collections.emptyMap();
        if (!completedTasks.isEmpty()) {
            taskImageMap = ugcRepository.findByImageTaskIn(completedTasks)
                    .stream()
                    .collect(Collectors.groupingBy(UGC::getImageTask));
        }

        List<TaskOrImageResponse> responses = new ArrayList<>();

        for (ImageTask task : tasks) {
            if (Status.IN_PROGRESS.equals(task.getStatus())) {
                responses.add(TaskOrImageResponse.fromTask(task));
            } else if (Status.COMPLETED.equals(task.getStatus())) {
                List<UGC> images = taskImageMap.getOrDefault(task, Collections.emptyList());
                for (UGC image : images) {
                    responses.add(TaskOrImageResponse.fromImage(task, image));
                }
            }
        }

        return responses;
    }
}

