package com.grtc.main.global.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

// 목록 API 공통 응답 (명세서 3-1 "페이징 응답의 data")
//   { "content": [ ], "page": 0, "size": 20, "totalElements": 135, "totalPages": 7 }
//   - from(...): 명세서 기준. page 는 0부터 시작한다. (회원관리, 차량관리, 배차관리)
//   - of(...)  : 예전 방식. page 가 1부터 시작한다. (아직 예전 주소를 쓰는 민원관리에서만 사용)
public record PageResponse<T>(
        List<T> content,       // 현재 페이지 데이터
        int page,              // 현재 페이지
        int size,              // 페이지 크기
        long totalElements,    // 전체 건수
        int totalPages         // 전체 페이지 수
) {

    // [명세서 기준, page 0부터] 엔티티 Page 를 DTO 로 변환해서 응답으로 만든다.
    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return from(page, page.getContent().stream().map(mapper).toList());
    }

    // [명세서 기준, page 0부터] 이미 변환한 목록(예: 행 번호를 붙인 목록)에 페이지 정보만 붙인다.
    public static <T> PageResponse<T> from(Page<?> page, List<T> content) {
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    // [예전 방식, page 1부터] 엔티티 Page 를 DTO 로 변환해서 응답으로 만든다.
    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return of(page, page.getContent().stream().map(mapper).toList());
    }

    // [예전 방식, page 1부터] 이미 변환한 목록에 페이지 정보만 붙인다.
    public static <T> PageResponse<T> of(Page<?> page, List<T> content) {
        return new PageResponse<>(
                content,
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
