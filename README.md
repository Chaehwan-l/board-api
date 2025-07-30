# 게시판 API (Board API)

## 설명

Spring Boot를 활용한 CRUD RESTful API 실습용 게시판 서버 프로젝트   
HTTP 메서드 이해, JPA 연동, DB 마이그레이션, Swagger, 간단 뷰(Thymeleaf) 등을 단계별로 학습하고 구현    

## 주요 기능

* 게시글 CRUD(Create, Read, Update, Delete)
* 페이징, 검색 기능 확장 가능
* Flyway를 이용한 DB 스키마 버전 관리
* Swagger UI를 통한 API 문서화 및 테스트
* 간단한 Thymeleaf 뷰(옵션)

## 기술 스택

* Java 21
* Spring Boot 3.5
* Spring Data JPA (Hibernate)
* Flyway
* MySQL
* Gradle
* SpringDoc OpenAPI (Swagger UI)
* Thymeleaf

## 초기 구상

1. 게시판 기본 기능 (글목록, 페이징, 글쓰기, 로그인, 댓글, 검색 등)
2. RESTful API 서버 구현 (Controller, Service, Repository 계층)
3. HTML 프론트(Thymeleaf) 연동 및 EC2/RDS 배포 실습
4. Flyway 마이그레이션 스크립트 작성 및 문서화
5. 기능 확장: S3 첨부파일, 좋아요, Spring Security 인증·인가 등

## 환경 설정

+ `.env` 파일 생성 (프로젝트 루트)

   ```dotenv
   DB_URL=jdbc:mysql://localhost:3306/board_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
   DB_USER=<username>
   DB_PASS=<password>
   ```

## 실행 방법

* Swagger UI: [http://localhost:8088/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## DB 마이그레이션

* `src/main/resources/db/migration/V1__create_post_table.sql` 파일을 통해 `post` 테이블 생성
* Flyway는 애플리케이션 시작 시 자동 실행

## API 예시

* 전체 조회: `GET /api/posts`
* 단건 조회: `GET /api/posts/{id}`
* 생성: `POST /api/posts`
* 수정: `PUT /api/posts/{id}`
* 삭제: `DELETE /api/posts/{id}`

## 향후 계획

* 페이징, 검색 기능 강화
* DTO/Validation 적용
* S3 기반 파일 업로드
* Spring Security 인증·인가
* AWS EC2/RDS 배포 가이드 추가

---
