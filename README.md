# 물류 관리 및 배송 시스템을 위한 MSA 기반 플랫폼 개발


## 📦 프로젝트 소개

MSA기반 **물류 관리 및 배송 시스템**입니다. \
서비스 간 API 연동, 데이터 무결성, 통신 신뢰성 확보를 목표로 Spring Cloud/Spring Boot로 구현했고, 일부 기능은 Spring AI + Gemini API로 구현했습니다. \
**핵심 흐름**: 업체(생산/수령)가 허브에 소속 → 상품·재고 등록 → 주문 발생 → 허브 라우트 기준 배송 경로 산정 → 배송 담당자 배정 및 배송 → Slack으로 진행상황 알림.

Eureka(서비스 디스커버리) + Gateway(API 게이트웨이)로 서비스를 등록·라우팅하고, \
서비스 간 호출은 OpenFeign(동기)과 RabbitMQ(비동기, 주문-배송 간 이벤트 연동)를 함께 사용하며, 장애 대응은 Resilience4j 서킷브레이커로 처리합니다.

## 👥 Team & Roles

| 이름 | 역할 | 담당 서비스                     |
|------|------|----------------------------|
| 최유준 | 리더 | 📦 주문 · 🚚 배송 · 👨‍💼 배송 담당자 |
| 안예지 | 부리더 | 🛍️ 상품 · 📦 재고 · 💬 슬랙 메시지 · ⚙️ 인프라|
| 김우태 | 멤버 | 🏢 허브                      |
| 장준영 | 멤버 | 👤 사용자                     |
| 최필규 | 멤버 | 🏭 업체 · 🤖 AI              |

## 🛠️ Tech Stack & Architecture

**공통**: Java 17 · Spring Boot 3.5.7 · Spring Cloud · Spring Data JPA · QueryDSL · PostgreSQL · Eureka · OpenFeign · Spring Security · Lombok · Zipkin(분산 트레이싱)

**서비스별 특이 스택**

| 서비스 | 특이사항 |
|---|---|
| gateway-service | WebFlux, OAuth2 Resource Server(JWT 검증) |
| eureka-server | 서비스 디스커버리 전용 |
| ai-service | Spring AI(Gemini/OpenAI), pgvector(RAG) |
| order-service, delivery-service | RabbitMQ(이벤트 연동) |
| user-service | JWT 발급(jjwt) |
| hub-service | Resilience4j 서킷브레이커 |

**아키텍처**

서비스 구성:
- 인프라: `eureka-server`, `gateway-service`
- 비즈니스: `hub-service`, `company-service`, `product-service`(재고 포함), `order-service`, `delivery-service`, `slack-service`, `user-service`, `ai-service`

기타:
- DB: 물리적으로 하나의 PostgreSQL 인스턴스, 서비스별 스키마 분리(`hub_schema`, `company_schema` 등)
- 통신: 동기 호출은 OpenFeign, 주문↔배송 간 비동기 이벤트는 RabbitMQ(outbox 패턴)
- 장애 대응: Resilience4j 서킷브레이커 + Fallback

## 🧩 주요 기능

| Domain | 기능 |
|---|---|
| 👤 User | 회원가입/승인 대기, 로그인, JWT 발급, 내 정보 조회·수정·탈퇴, 관리자 회원 관리(CRUD) |
| 🌐 Gateway | API 라우팅, JWT 검증 |
| 📍 Hub | 허브 CRUD|
| 🚚 Hub Route | 허브 간 이동 경로 CRUD, 중앙허브 경유 경로 산정 |
| 🏢 Company | 업체 CRUD |
| 📦 Product | 상품 CRUD |
| 📊 Inventory | 재고 조회, 입고/주문차감/주문취소복원 유형별 재고 증감 |
| 🛒 Order | 주문 생성·조회, 주문-배송 상태 통합 조회, 배송 실패 시 자동 취소(Saga 보상) |
| 🚛 Delivery | 주문 이벤트 기반 배송 생성, 배송 상태 관리, 배송 삭제 |
| 🛣 Delivery Route | 허브 구간별 배송 경로 조회 |
| 👷 Delivery Manager | 배송 담당자 등록·삭제(논리삭제), 순번 기반 배정 |
| 🤖 AI | 배송 최종 발송 시한 예측 및 Slack 알림 발송, 분석 이력 조회·검색·삭제 |
| 💬 Slack | 배송 담당자 알림 메시지 생성·조회·검색·수정·삭제 |

## 🚀 실행 방법(로컬)

**0. `.env` 준비**: 레포 루트에 `.env` 필요. 필요한 변수:

| 변수 | 용도 |
|---|---|
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | DB 계정 |
| `POSTGRES_HOST_PORT` | 로컬에서 접속할 Postgres 포트 |
| `AI_API_KEY` | ai-service AI 모델 API 키 |
| `JWT_SECRET` | gateway-service JWT 검증용 |
| `SECURITY_JWT_SECRET` | user-service JWT 발급용 |
| `SLACK_BOT_TOKEN` | slack-service 봇 토큰 |
| `SLACK_TEST_USER_ID` | slack-service 테스트 수신자 ID |

가장 간단한 방법은 팀에 공유된 `.env` 파일을 그대로 사용하는 것입니다(위 표는 직접 구성해야 할 경우 참고용).

**1. 빌드+기동**
```bash
docker compose up -d --build
```
postgres healthy 이후 나머지 서비스 순서대로 기동, 스키마는 최초 1회만 자동 생성(`docker/postgres/init/01-schemas.sql`). 다시 만들려면 `docker compose down -v`.

**2. 상태 확인**
```bash
docker compose ps
docker compose logs -f <service-name>
```

**3. 마스터 계정 생성 (최초 1회)**: 회원가입 API로는 MASTER 생성이 막혀있어 SQL 직접 삽입 필요.
```sql
INSERT INTO user_schema.p_users
  (user_id, login_id, password, name, email, phone, role, signup_status, slack_id, hub_id, supplier_id, created_at, created_by, is_deleted)
VALUES
  (gen_random_uuid(), 'master01', '$2a$10$5Ss.fUoX/OEP4.N7HBhbXu0OG4nMkH7Xy.WvQ28UWx49kPWZXbIba', '마스터관리자', 'master@example.com', '010-0000-0000', 'MASTER', 'APPROVED', NULL, NULL, NULL, now(), gen_random_uuid(), false);
```
로그인: `master01` / `Master1234!`


## 🗂️ ERD
[ERD Cloud에서 보기](https://www.erdcloud.com/d/kStzmtvQ4u558psqa)
