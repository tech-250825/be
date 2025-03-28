package com.ll.demo03.domain.task.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequestMessage implements Serializable {
    private String gptPrompt;
    private String ratio;
    private String cref;
    private String sref;
    private String webhookUrl;
    private Long memberId;
    private String rawPrompt;

}
