/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-04
 * 설명: 조회 API 페이지 응답 통일
 {
    "content": [
        {
            "userId": "1",
            "nickname": "user1"
        },
        {
            "userId": "2",
            "nickname": "user2"
        },
    ],
    "page": 0,
    "size": 10,
    "totalElements": 23,
    "totalPages": 3
 }
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */

package com.sparta.userservice.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final long totalPages;

    // <Entity> -> <해당 Entity의 Response> 매핑해서 list로 반환
    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> converter) {
        List<T> content = page.getContent().stream()
                .map(converter).toList();
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
