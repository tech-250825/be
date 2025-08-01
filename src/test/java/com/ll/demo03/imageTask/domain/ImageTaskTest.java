package com.ll.demo03.imageTask.domain;

import com.ll.demo03.global.domain.Status;
import com.ll.demo03.imageTask.domain.ImageTask;
import com.ll.demo03.member.domain.AuthProvider;
import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.domain.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ImageTaskTest {

    @Test
    public void StatusUpdate로_상태변경을_할_수_있다() {

        Member creator =  Member.builder()
                .id(1L)
                .email("ninetale22@gmail.com")
                .name("한유림")
                .profile("profile.url")
                .credit(2000)
                .role(Role.USER)
                .provider(AuthProvider.GOOGLE)
                .providerId("12344321")
                .build();

        ImageTask task = ImageTask.builder()
                .id(1L)
                .prompt("prompt")
                .lora("lora")
                .status(Status.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now()) //일단 시간은 db에서 저장되므로 쩔수없 ㅠ
                .creator(creator)
                .build();
        task = task.updateStatus(Status.COMPLETED, "12345678");

        assertThat(task.getStatus()).isEqualTo(Status.COMPLETED);
        assertThat(task.getRunpodId()).isEqualTo("12345678");
        assertThat(task.getCreator()).isSameAs(creator);

    }




}
