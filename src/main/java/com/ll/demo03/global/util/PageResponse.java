package com.ll.demo03.global.util;

public record PageResponse<T>(
        T content,
        String previousPageCursor,
        String nextPageCursor
) { }
