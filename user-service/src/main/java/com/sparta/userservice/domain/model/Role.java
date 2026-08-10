package com.sparta.userservice.domain.model;

public enum Role {

    SUPPLIER_AGENT(Authority.SUPPLIER_AGENT),     // 생산 업체 담당자
    DELIVERY_AGENT(Authority.DELIVERY_AGENT),     // 배송 담당자
    HUB_MANAGER(Authority.HUB_MANAGER),           // 허브 관리자
    MASTER(Authority.MASTER);                     // 최고 관리자

    private final String authority; // Spring Security에서 사용하는 권한 문자열과 연결하기 위한 변수

    Role(String authority) { this.authority = authority; }

    public String getAuthority() { return authority; }

    public static class Authority {

        public static final String SUPPLIER_AGENT = "ROLE_SUPPLIER_AGENT";
        public static final String DELIVERY_AGENT = "ROLE_DELIVERY_AGENT";
        public static final String HUB_MANAGER = "ROLE_HUB_MANAGER";
        public static final String MASTER = "ROLE_MASTER";

        private Authority() { } // 문자열 상수만 있는 거기에, 다른 클래스에서 객체 생성을 방지한다.
    }
}