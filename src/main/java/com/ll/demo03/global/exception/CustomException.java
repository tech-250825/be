package com.ll.demo03.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.ll.demo03.global.error.ErrorCode;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}