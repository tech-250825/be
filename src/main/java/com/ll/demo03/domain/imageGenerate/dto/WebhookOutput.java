package com.ll.demo03.domain.imageGenerate.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class WebhookOutput {
    private String image_url;
    private List<String> image_urls;
    private List<String> temporary_image_urls;
    private String discord_image_url;
    private List<String> actions;
    private Integer progress;
    private List<String> intermediate_image_urls;
}
