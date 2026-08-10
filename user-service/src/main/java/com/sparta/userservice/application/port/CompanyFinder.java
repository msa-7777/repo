package com.sparta.userservice.application.port;

import java.util.UUID;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 장준영
 * 작성일: 2026-08-06
 * 설명: 업체를 연결하기 전 외부 업체 서비스에서 해당 업체의 존재 여부를 확인하기 위한 출력 포트
 * ─────────────────────────────────────────────────────────────────────────────────────────────────
 */

public interface CompanyFinder {
    void validateCompanyExists(UUID companyId);
}
