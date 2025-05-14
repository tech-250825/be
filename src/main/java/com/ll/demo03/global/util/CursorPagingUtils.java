package com.ll.demo03.global.util;

import org.springframework.data.domain.Slice;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 커서 기반 페이지네이션을 위한 유틸리티 클래스
 */
public class CursorPagingUtils {

    /**
     * 엔티티 리스트를 응답 객체로 변환하고 커서 기반 페이징 응답을 생성
     *
     * @param <E> 엔티티 타입
     * @param <R> 응답 타입
     * @param slice 페이징된 엔티티 슬라이스
     * @param entities 엔티티 리스트
     * @param responseMapper 엔티티를 응답 객체로 변환하는 함수
     * @param idExtractor 엔티티에서 ID를 추출하는 함수
     * @param hasPreviousCheck 이전 페이지 존재 여부를 확인하는 함수 (첫 ID, 추가 파라미터)
     * @param pageable 커서 기반 페이지 정보
     * @param additionalParam hasPreviousCheck 함수에 전달할 추가 파라미터 (예: memberId)
     * @return 커서 기반 페이징 응답
     */
    public static <E, R, P> PageResponse<List<R>> createPageResponse(
            Slice<E> slice,
            List<E> entities,
            Function<E, R> responseMapper,
            Function<E, String> idExtractor,
            BiFunction<String, P, Boolean> hasPreviousCheck,
            CursorBasedPageable pageable,
            P additionalParam
    ) {
        if (!slice.hasContent()) {
            return new PageResponse<>(Collections.emptyList(), null, null);
        }

        // 엔티티 리스트를 응답 객체로 변환
        List<R> responseList = entities.stream()
                .map(responseMapper)
                .toList();

        // 첫 번째 아이템의 ID
        String firstId = idExtractor.apply(entities.get(0));

        // 마지막 아이템의 ID
        String lastId = idExtractor.apply(entities.get(entities.size() - 1));

        // 이전 페이지 커서 계산
        String previousPageCursor = null;
        if (hasPreviousCheck.apply(firstId, additionalParam)) {
            previousPageCursor = pageable.getEncodedCursor(firstId, true);
        }

        // 다음 페이지 커서 계산
        String nextPageCursor = null;
        if (slice.hasNext()) {
            nextPageCursor = pageable.getEncodedCursor(lastId, true);
        }

        return new PageResponse<>(responseList, previousPageCursor, nextPageCursor);
    }

    /**
     * 간단한 경우의 페이징 응답 생성 (이전 페이지 확인을 위한 추가 파라미터가 필요 없는 경우)
     */
    public static <E, R> PageResponse<List<R>> createPageResponse(
            Slice<E> slice,
            List<E> entities,
            Function<E, R> responseMapper,
            Function<E, String> idExtractor,
            Function<String, Boolean> hasPreviousCheck,
            CursorBasedPageable pageable
    ) {
        return createPageResponse(
                slice,
                entities,
                responseMapper,
                idExtractor,
                (id, param) -> hasPreviousCheck.apply(id),
                pageable,
                null
        );
    }
}