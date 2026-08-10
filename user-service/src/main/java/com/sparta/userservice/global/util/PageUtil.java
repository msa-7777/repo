/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-04
 * 설명: 페이지 번호, 페이지 크기, 정렬 조건을 검증해 Spring Data JPA의 Pageable 객체로 만들어주는 Utility Class
 *          Utility Class => 객체의 상태를 저장하는 클래스가 아닌, 여러 곳에서 공통으로 쓸 기능을 모아둔 클래스
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */
package com.sparta.userservice.global.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtil {

    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_FIELD = "createdAt";

    // 클래스 이름으로 정적 메서드만 호출하는 util class이기에, 접근 범위를 private로 지정해 instance 생성을 막는다
    private PageUtil() {}

    public static Pageable toPageable(int page, int size, String sort) {
        return toPageable(page, size, sort, DEFAULT_SORT_FIELD);
    }
    public static Pageable toPageable(int page, int size, String sort, String sortField) {
        // TODO: (프론트엔드 처리 또는 클라이언트 요청에서 음수가 들어올 수 있음) -> page의 첫 counting인 0으로 setting
        int validatedPage = (page >= 0 ? page : 0);
        int validatedSize = validatedSize(size);
        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(validatedPage, validatedSize, Sort.by(direction, sortField));
    }

    public static Pageable toPageable(int page, int size, Sort.Order... orders) {
        int validatedPage = (page >= 0 ? page : 0);
        int validatedSize = validatedSize(size);
        // TODO: 호출할 때 Sort.Order.desc("createdAt"), Sort.Order.asc("name") 다음과 같이 설정 가능하다
        Sort sort = Sort.by(orders);

        return PageRequest.of(validatedPage, validatedSize, sort); // // 2개 이상의 정렬 규칙 지원
    }

    private static int validatedSize(int size) {
        return (size == 10 || size == 30 || size == 50) ? size : DEFAULT_SIZE;
    }
}
