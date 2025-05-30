package com.ll.demo03.domain.image.entity;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.folder.entity.Folder;
import com.ll.demo03.domain.task.entity.Task;
import com.ll.demo03.domain.upscaledTask.entity.UpscaleTask;
import com.ll.demo03.domain.videoTask.entity.VideoTask;
import com.ll.demo03.global.base.BaseEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name="image")
public class Image extends BaseEntity {

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    @Nullable
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    private int imgIndex;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upscaleTask_id")
    private UpscaleTask upscaleTask;

    private int likeCount;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "videoTask_id")
    private VideoTask videoTask;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    private Boolean isShared = false;

    private Boolean isUpscaled = false;

    public static Image of(String url, Task task) {
        return Image.builder()
                .url(url)
                .task(task)
                .member(task.getMember())
                .build();
    }

    public static Image ofUpscale(String url, Task task, UpscaleTask upscaleTask) {
        return Image.builder()
                .url(url)
                .task(task)
                .upscaleTask(upscaleTask)
                .member(upscaleTask.getMember())
                .build();
    }

    public static Image ofVideo(String url, VideoTask videoTask) {
        return Image.builder()
                .url(url)
                .videoTask(videoTask)
                .member(videoTask.getMember())
                .build();
    }

}