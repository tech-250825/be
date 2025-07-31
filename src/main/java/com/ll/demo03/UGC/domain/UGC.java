package com.ll.demo03.UGC.domain;

import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.videoTask.domain.VideoTask;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UGC {

    private Long id;
    private String url;
    private int index;
    private ImageTask imageTask;
    private VideoTask videoTask;
    private LocalDateTime createdAt;
    private Member creator;

    @Builder
    public UGC(Long id, String url, int index, ImageTask imageTask, VideoTask videoTask, LocalDateTime createdAt, Member creator){
        this.id = id;
        this.url = url;
        this.index = index;
        this.imageTask = imageTask;
        this.videoTask = videoTask;
        this.createdAt = createdAt;
        this.creator = creator;
    }

    public static UGC ofImage(String url, ImageTask imageTask, int index) {
        return UGC.builder()
                .url(url)
                .index(index)
                .imageTask(imageTask)
                .creator(imageTask.getCreator())
                .build();
    }

    public static UGC ofVideo(String url, com.ll.demo03.videoTask.domain.VideoTask videoTask) {
        return UGC.builder()
                .url(url)
                .videoTask(videoTask)
                .index(0)
                .creator(videoTask.getCreator())
                .build();
    }

}
