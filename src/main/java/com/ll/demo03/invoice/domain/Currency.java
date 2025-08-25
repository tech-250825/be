package com.ll.demo03.invoice.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Currency {
    USDT;

    @JsonCreator
    public static Currency from(String value) {
        return Currency.valueOf(value.toUpperCase());
    }
}
