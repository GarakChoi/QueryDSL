# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소의 코드를 작업할 때 참고하는 가이드입니다.

## 아키텍처

이는 OAuth2 인증을 포함한 게시물/댓글 시스템을 위한 REST API를 제공하는 Kotlin으로 작성된 Spring Boot 3.5.4 애플리케이션입니다.

### 핵심 기술 스택

- **백엔드**: Spring Boot 3.5.4 (Kotlin)
- **데이터베이스**: H2 (개발용 인메모리)
- **보안**: Spring Security with JWT and OAuth2 (카카오, 구글, 네이버)
- **문서화**: SpringDoc OpenAPI 3
- **빌드 도구**: Gradle with Kotlin DSL

### 패키지 구조

```
com.back/
├── domain/           # 비즈니스 도메인 모듈
│   ├── member/       # 사용자 인증 및 관리
│   ├── post/         # 게시물 및 댓글
│   └── home/         # 홈 페이지 컨트롤러
├── global/           # 공통 관심사
│   ├── security/     # 인증, OAuth2, JWT
│   ├── exception/    # 전역 예외 처리
│   ├── jpa/          # JPA 기본 엔티티
│   ├── app/          # 설정
│   └── aspect/       # AOP 측면
└── standard/         # 유틸리티 및 확장
```

### 주요 구성 요소

#### 인증 시스템

- `AuthTokenService`를 통한 JWT 기반 인증
- 카카오, 구글, 네이버를 위한 OAuth2 통합
- `global.security`의 커스텀 보안 필터
- 역할 기반 접근 제어 (USER, ADMIN)

#### 도메인 아키텍처

각 도메인은 동일한 패턴을 따릅니다:

- **Controller**: REST API 엔드포인트 (`ApiV1*Controller`)
- **Service**: 비즈니스 로직
- **Repository**: 데이터 접근 (Spring Data JPA)
- **Entity**: `BaseEntity`를 확장하는 JPA 엔티티
- **DTO**: 데이터 전송 객체

#### 응답 구조

모든 API 응답은 `RsData<T>` 래퍼를 사용합니다:

- `resultCode`: 응답 상태 코드
- `msg`: 사람이 읽을 수 있는 메시지
- `data`: 응답 페이로드

### 설정 참고사항

- 활성 프로필: `dev` (`application.yml`에서 설정)
- 데이터베이스 자동 생성 활성화 (`ddl-auto: update`)
- localhost:3000과 cdpn.io에 대한 CORS 설정
- 환경 변수에서 OAuth2 클라이언트 자격 증명 로드
- dev/test 프로필에서 테스트 데이터 자동 시드

### 테스트 구조

- 테스트는 Java로 작성 (Kotlin이 아닌)
- `src/test/java/`에 위치
- controller-service-repository 테스트 패턴 따름
- Spring Boot Test 프레임워크 사용