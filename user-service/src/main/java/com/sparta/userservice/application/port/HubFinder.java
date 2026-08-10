package com.sparta.userservice.application.port;

import java.util.UUID;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-06
 * 설명: 허브를 연결하기 전 외부 허브 서비스에서 해당 허브의 존재 여부를 확인하기 위한 출력 포트

 UserService
    ↓ 사용
 HubFinder
    ↑ 구현
 HubFinderImpl
    ↓ 호출
 HubClient
    ↓
 hub-service

 HubFinder
    → 인터페이스
    → application이 외부로 나가기 위해 사용하는 출력 포트

 HubFinderImpl
    → 출력 포트 구현체
    → infrastructure의 출력 어댑터
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

public interface HubFinder {
    void validateHubExists(UUID hubId);
}
