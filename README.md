# 게시판 API (Board API)

## 개요

Spring Boot로 구현한 학습용 게시판 서버    
웹 뷰(Thymeleaf) 중심으로 CRUD, 댓글, 파일 첨부(S3), 폼 로그인과 OAuth2 로그인을 제공합니다. 스키마 관리는 Flyway로 수행

## 구현 범위

* 게시글: 목록, 상세, 작성, 수정, 삭제

  * 페이징, 제목/작성자 검색
* 댓글: 작성, 삭제
* 첨부파일: 드래그앤드롭 임시 업로드 → 저장 시 게시글로 마이그레이션

  * S3 Presigned URL 기반 다운로드(다운로드, 인라인)
* 인증/인가

  * 폼 로그인 + 회원가입
  * OAuth2(GitHub, Google, Naver) 로그인 후 추가 정보 입력 완료 플로우(`/register/complete`)
  * 작성자 또는 ADMIN만 수정/삭제 가능
* 스키마 관리: Flyway 마이그레이션 스크립트 일괄 적용

## 기술 스택

* Java 21, Spring Boot 3.5.4, Gradle
* Spring Web, Thymeleaf, Spring Data JPA, Spring Security, OAuth2 Client
* Flyway, MySQL
* AWS SDK for Java v2(S3, S3Presigner)
* Test: JUnit 5, Spring Security Test, Mockito, H2

## 실행 전제

* JDK 21, Gradle
* MySQL 접근 가능 (User 권한 확인)
* S3 사용 시 AWS 자격 증명 프로필 또는 기본 자격 증명 체인

## 환경 변수(.env)

```dotenv
# DB
DB_URL=jdbc:mysql://localhost:3306/board_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&createDatabaseIfNotExist=true
DB_USER=<username>
DB_PASS=<password>

# 초기 ADMIN 계정(Flyway에서 주입)
ADMIN_USER=<admin>
ADMIN_EMAIL=<admin@example.com>
ADMIN_PW_HASH=<bcrypt_hash>
ADMIN_ROLE=ADMIN

# OAuth2
GITHUB_CLIENT_ID=<id>
GITHUB_CLIENT_SECRET=<secret>
GOOGLE_CLIENT_ID=<id>
GOOGLE_CLIENT_SECRET=<secret>
NAVER_CLIENT_ID=<id>
NAVER_CLIENT_SECRET=<secret>

# AWS S3
AWS_REGION=<ap-northeast-2>
AWS_PROFILE=<aws_profile_or_empty>
S3_BUCKET=<bucket_name>
```

> `spring.config.import=optional:file:.env[.properties]` 로딩. 실제 값은 본인 자격 증명으로 교체

## 빌드 및 실행

```bash
./gradlew bootRun   # 서버 시작
./gradlew test      # 테스트 실행
```

기본 포트: `8088`

## 주요 URL

* 메인: `GET /`
* 로그인/로그아웃: `GET /login`, `POST /logout`
* 회원가입: `GET /register`, `POST /register`
* OAuth2 완료 플로우: `GET /register/complete`
* 게시글

  * 목록/검색: `GET /posts?type=title|author&q=...&page=0&size=10`
  * 상세: `GET /posts/{id}`
  * 작성 폼: `GET /posts/new`
  * 저장: `POST /posts`
  * 수정 폼: `GET /posts/{id}/edit`
  * 수정: `PUT /posts/{id}` (뷰 폼은 hidden \_method 로 오버라이드)
  * 삭제: `POST /posts/{id}/delete`
* 댓글

  * 등록: `POST /posts/{postId}/comments`
  * 삭제: `DELETE /comments/{id}`
* 첨부

  * 임시 업로드: `POST /api/uploads/draft` (multipart, `draftId`, `file`)
  * 임시 삭제: `DELETE /api/uploads/draft?draftId=...&key=...`
  * 마이그레이션: 게시글 저장 시 서버에서 `drafts/{draftId}/...` → `posts/{postId}/...`
  * 다운로드: `GET /attachments/{id}/download`
  * 인라인 보기: `GET /attachments/{id}/inline`
  * 삭제: `DELETE /attachments/{id}`

## 데이터 모델(요약)

* `user_account`: username, email, password, role(default USER), provider, provider\_id, signup\_completed, created\_at
* `post`: title, content, user\_id(FK), created\_at
* `attachment`: post\_id(FK, nullable), s3\_key, original\_name, content\_type, size, created\_at
* `comment`: post\_id(FK), user\_id(FK), content, created\_at

## 보안/권한

* 로그인 필요 시 미인증 접근은 `/register?required` 로 리다이렉션
* 작성자 또는 ADMIN만 게시글/댓글 삭제와 수정 가능
* CSRF 활성화, `spring.mvc.hiddenmethod.filter.enabled=true` 로 PUT/DELETE 폼 지원

## 테스트

* 리포지토리, 서비스, 컨트롤러 WebMvc 테스트 포함
* H2를 테스트 런타임에 사용

## 배포 참고

* S3 클라이언트와 Presigner는 `AWS_REGION`, `AWS_PROFILE`을 사용

  * `AWS_PROFILE`이 비어 있으면 기본 자격 증명 체인을 사용
* Flyway가 시작 시 마이그레이션과 초기 ADMIN 계정 삽입 수행


