package com.ll.demo03.global.port;

import java.util.List;

public interface ResponseConverter<T, R> {
    List<R> convertToResponses(List<T> entities);
}
