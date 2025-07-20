package com.ll.demo03.videoTask.infrastructure;

import com.ll.demo03.global.domain.Status;
import com.ll.demo03.member.infrastructure.MemberEntity;
import com.ll.demo03.videoTask.domain.VideoTask;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "video_tasks")
@EntityListeners(AuditingEntityListener.class)
public class VideoTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String prompt;

    private String lora;

    private String runpodId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @CreatedDate
    @Column(name= "created_at")
    private LocalDateTime createdAt;

    @CreatedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    public static VideoTaskEntity from(VideoTask task) {
        VideoTaskEntity taskEntity = new VideoTaskEntity();
        taskEntity.id = task.getId();
        taskEntity.prompt = task.getPrompt();
        taskEntity.lora = task.getLora();
        taskEntity.runpodId = task.getRunpodId();
        taskEntity.createdAt = task.getCreatedAt();
        taskEntity.modifiedAt = task.getModifiedAt();
        taskEntity.status = task.getStatus();
        taskEntity.member = MemberEntity.from(task.getCreator());

        return taskEntity;
    }

    public VideoTask toModel() {
        return VideoTask.builder()
                .id(id)
                .prompt(prompt)
                .lora(lora)
                .runpodId(runpodId)
                .status(status)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .creator(member.toModel())
                .build();
    }
}