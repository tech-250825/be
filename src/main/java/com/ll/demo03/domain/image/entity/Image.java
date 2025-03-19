package com.ll.demo03.domain.image.entity;
import com.ll.demo03.domain.member.entity.Member;
import com.ll.demo03.domain.task.entity.Task;
import com.ll.demo03.global.base.BaseEntity;
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

    @Builder.Default
    private Boolean isBookmarked = false;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    private int imgIndex;

    public static Image of(String url, Task task) {
        return Image.builder()
                .url(url)
                .task(task)
                .member(task.getMember())
                .build();
    }

    public boolean isBookmarked() {
        return Boolean.TRUE.equals(isBookmarked);
    }

    public void toggleBookmark() {
        this.isBookmarked = !this.isBookmarked();
    }

}