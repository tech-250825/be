package com.ll.demo03.global.domain;

public class PageCursors {
    private final String prevCursor;
    private final String nextCursor;

    public PageCursors(String prevCursor, String nextCursor) {
        this.prevCursor = prevCursor;
        this.nextCursor = nextCursor;
    }

    public String getPrevCursor() {
        return prevCursor;
    }

    public String getNextCursor() {
        return nextCursor;
    }
}
