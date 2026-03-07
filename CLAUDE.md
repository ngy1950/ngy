# 대원 케이터링 웹사이트

## 프로젝트 개요
아버지 한식 케이터링 사업 홍보 사이트. 제사/장지/단체음식 메뉴 소개 + 갤러리/리뷰 게시판.

## 기술 스택
- Spring Boot 3.2.2 + Thymeleaf + Java 17 + Gradle
- Spring Security (관리자 로그인)
- Spring Data JPA + PostgreSQL (Railway)
- Cloudinary (이미지 저장)
- Bootstrap 5 + Font Awesome
- 배포: Railway (Dockerfile 기반)

## 주요 경로
- 템플릿: `src/main/resources/templates/thymeleaf/`
- 공통 fragment: `fragments/` (header, footer, head, subpage)
- 메뉴 페이지: `menu/` (jesa, jangji, group, gallery, gallery-detail)
- 관리자 페이지: `admin/` (login, dashboard, upload)
- Java: `src/main/java/com/study/ngy/`

## 패키지 구조
```
com.study.ngy
├── config/          SecurityConfig, CloudinaryConfig
├── domain/gallery/  GalleryPost, GalleryImage, Repository, GalleryService
├── domain/review/   Review, ReviewRepository, ReviewService
├── web/             GalleryController, ReviewController, AdminController
│   └── viewController/ ViewController (/, /menu/*)
└── util/            CloudinaryService
```

## URL 구조
| URL | 설명 |
|-----|------|
| `/` | 메인 |
| `/menu/jesa`, `/menu/jangji`, `/menu/group` | 메뉴 페이지 |
| `/gallery` | 갤러리 목록 (카테고리 필터) |
| `/gallery/{id}` | 갤러리 상세 + 리뷰 |
| `/gallery/{id}/review` | 리뷰 등록 POST |
| `/admin/login` | 관리자 로그인 |
| `/admin/dashboard` | 게시물 관리 |
| `/admin/gallery/new` | 사진 업로드 |

## 환경변수 (Railway)
- `SPRING_DATASOURCE_URL` / `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`
- `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET`
- `ADMIN_USERNAME` / `ADMIN_PASSWORD`

## 로컬 개발
- `application.properties` — H2 인메모리 DB, gitignore됨 (민감정보 포함)
- `application-prod.properties` — Railway 배포용, git 추적됨
- 로컬 실행: `./gradlew bootRun` → http://localhost:8080

## 배포
- git push → Railway 자동 배포
- Dockerfile: 2단계 빌드 (jdk-alpine → jre-alpine)
- `--rerun-tasks` 옵션으로 Gradle 캐시 우회 중

## 실제 사업 정보
- 대표: 남태우 / 전화: 010-8916-7074
- 주소: 충북 청주시 흥덕구 비하로 27
- 카카오채널: _cEzZX

## 주요 결정사항
- 리뷰 승인 기능 현재 비활성화 (즉시 게시) — ReviewService.addReview() 참고
- DB 드라이버 명시 필요 (H2/PostgreSQL 충돌 이력)
- Railway Variables UI 버그로 PGHOST 대신 SPRING_DATASOURCE_URL 사용

## 사이트 철학 (기능/디자인 작업 시 항상 참고)

> **"결제 기능이 없는 상담형 사이트는 신뢰가 전부다.
> 디자인을 세련되게 만드는 것보다,
> '우리는 남들처럼 공장에서 찍어내는 음식이 아니라,
> 당신의 제사를 위해 정성을 다한다'는 메시지를 시각화하는 데 집중하라."**

### 이 철학을 적용하는 원칙

**신뢰 > 세련됨**
- 새 기능/디자인을 추가할 때 "이게 신뢰를 높이는가?"를 먼저 질문할 것
- 화려한 애니메이션보다 실제 후기, 실제 사진, 실제 사람이 더 효과적

**"사람이 만든다"를 증명하는 요소 우선**
- 대표 남태우의 얼굴·경력·자격을 노출하는 방향으로 발전
- 공정·기계·대량생산과 대비되는 표현 사용: 당일 새벽 구매, 손조리, 직접 포장 확인
- 제사/장지 음식의 특성상 "실수가 허용되지 않는 날"임을 인식한 메시지

**고객 후기가 핵심 콘텐츠**
- 갤러리 리뷰가 쌓일수록 사이트 신뢰도가 높아지는 구조
- 홈에서도 최근 리뷰 3개 노출 중 (ViewController → index.html)
- 후기 작성 진입 장벽을 낮추는 방향으로 UX 개선

**문의(전화/카카오) 유도가 최종 목표**
- 모든 페이지에서 전화번호(010-8916-7074)와 카카오채널(_cEzZX)이 쉽게 눈에 띄어야 함
- CTA 버튼은 항상 "지금 바로 상담" 방향으로


<!-- 
대화 시작 시 /clear — 이전 대화 컨텍스트 초기화. 새 주제면 새로 시작하는 게 효율적
질문을 구체적으로 — "갤러리 고쳐줘" 보다 "gallery-detail.html의 썸네일 클릭 버그 수정"이 토큰 낭비가 적음
파일 하나씩 — 여러 파일 동시 수정 요청보다 하나씩이 컨텍스트 덜 차지함
/compact — 대화가 길어지면 이전 내용을 요약해서 컨텍스트 줄여줌 
-->