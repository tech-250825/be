package com.ll.demo03.imageTask.infrastructure;

import com.ll.demo03.global.domain.Status;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.member.infrastructure.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "image_tasks")
public class ImageTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String prompt;

    private String lora;

    private String runpodId;

    private Status status;

    @CreatedDate
    @Column(name= "created_at")
    private LocalDateTime createdAt;

    @CreatedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private MemberEntity creator;

    public static ImageTaskEntity from(ImageTask imageTask) {
        ImageTaskEntity imageTaskEntity = new ImageTaskEntity();
        imageTaskEntity.id = imageTask.getId();
        imageTaskEntity.lora = imageTask.getLora();
        imageTaskEntity.prompt = imageTask.getPrompt();
        imageTaskEntity.runpodId = imageTask.getRunpodId();
        imageTaskEntity.status = imageTask.getStatus();
        imageTaskEntity.creator = MemberEntity.from(imageTask.getCreator());

        return imageTaskEntity;
    }

    public ImageTask toModel() {
        return ImageTask.builder()
                .id(id)
                .prompt(prompt)
                .lora(lora)
                .runpodId(runpodId)
                .status(status)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .creator(creator.toModel())
                .build();
    }
}