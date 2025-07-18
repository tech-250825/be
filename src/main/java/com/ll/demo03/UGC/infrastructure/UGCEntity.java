package com.ll.demo03.UGC.infrastructure;
import com.ll.demo03.UGC.domain.UGC;
import com.ll.demo03.imageTask.infrastructure.ImageTaskEntity;
import com.ll.demo03.member.infrastructure.MemberEntity;
import com.ll.demo03.videoTask.infrastructure.VideoTaskEntity;
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
@Table(name="UGC")
public class UGCEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    private int index;

    @Nullable
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private ImageTaskEntity imageTask;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "videoTask_id")
    private VideoTaskEntity videoTask;

    @Column(name= "created_at")
    private Long createdAt;

    @Column(name = "modified_at")
    private Long modifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private MemberEntity creator;

    public static UGCEntity from(UGC ugc) {
        UGCEntity ugcEntity = new UGCEntity();
        ugcEntity.id = ugc.getId();
        ugcEntity.url = ugc.getUrl();
        ugcEntity.index = ugc.getIndex();
        ugcEntity.imageTask = ImageTaskEntity.from(ugc.getImageTask());
        ugcEntity.videoTask = VideoTaskEntity.from(ugc.getVideoTask());
        ugcEntity.createdAt = ugc.getCreatedAt();
        ugcEntity.modifiedAt = ugc.getModifiedAt();
        ugcEntity.creator = MemberEntity.from(ugc.getCreator());

        return ugcEntity;
    }

    public UGC toModel() {
        return UGC.builder()
                .id(id)
                .url(url)
                .index(index)
                .imageTask(imageTask.toModel())
                .videoTask(videoTask.toModel())
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .creator(creator.toModel())
                .build();
    }
}