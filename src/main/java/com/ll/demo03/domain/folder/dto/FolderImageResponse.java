package com.ll.demo03.domain.folder.dto;


import com.ll.demo03.domain.image.dto.ImageResponse;
import com.ll.demo03.global.util.PageResponse;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class FolderImageResponse {
    private Long id;
    private String name;
    private PageResponse<List<ImageResponse>> images;
}
