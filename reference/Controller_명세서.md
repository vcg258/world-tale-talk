# World Tale Talk — Controller 명세서

| 항목 | 내용 |
|------|------|
| 버전 | v1.0 |
| 기준 문서 | `WorldChatRPG_요구사항정의서.docx`, `world-tale-talk DB 설계서.docx` |
| 기준 화면 | `src/main/resources/templates/**` |
| 뷰 상수 | `com.worldtaletalk.view.ViewPaths` |
| 작성일 | 2026-05-29 |

---

## 1. 개요

본 문서는 **Thymeleaf 서버 사이드 렌더링(SSR)** 용 `@Controller` 명세입니다.  
각 엔드포인트는 HTML 페이지를 반환하거나, 폼 POST 후 **Redirect** 합니다.

| 구분 | 설명 |
|------|------|
| Page Controller | HTML + Model -> Thymeleaf |
| 권한 | `permitAll` / `authenticated` / `hasRole('ADMIN')` (Spring Security 적용 예정) |
| 실시간 스토리 | WebSocket은 [API_명세서.md](./API_명세서.md) 참고 |

### 1.1 권장 Controller 패키지 구조

| Controller 클래스 | 패키지 | 담당 도메인 |
|-------------------|--------|-------------|
| `HomeController` | `com.worldtaletalk.view.home` | 메인, 공지 요약 |
| `AuthController` | `com.worldtaletalk.view.auth` | 로그인, 닉네임, 정지 안내 |
| `WorldViewController` | `com.worldtaletalk.view.world` | 세계관 목록/상세 |
| `PersonaViewController` | `com.worldtaletalk.view.persona` | 페르소나 선택 |
| `StoryViewController` | `com.worldtaletalk.view.story` | 플레이, 엔딩, 에필로그 |
| `MypageController` | `com.worldtaletalk.view.mypage` | 마이페이지 |
| `NoticeViewController` | `com.worldtaletalk.view.notice` | 공지 |
| `RatingViewController` | `com.worldtaletalk.view.rating` | 평점/리뷰 |
| `ReportViewController` | `com.worldtaletalk.view.report` | 신고 |
| `AdminDashboardController` | `com.worldtaletalk.view.admin` | 관리자 대시보드/통계 |
| `AdminUserController` | `com.worldtaletalk.view.admin` | 회원 관리 |
| `AdminWorldController` | `com.worldtaletalk.view.admin` | 세계관 관리 |
| `AdminPersonaController` | `com.worldtaletalk.view.admin` | 페르소나 관리 |
| `AdminItemController` | `com.worldtaletalk.view.admin` | 아이템 등급/확률 |
| `AdminSessionController` | `com.worldtaletalk.view.admin` | 세션 관리 |
| `AdminRatingController` | `com.worldtaletalk.view.admin` | 평점/리뷰 관리 |
| `AdminNoticeController` | `com.worldtaletalk.view.admin` | 공지 관리 |

---

## 2. 공통 규칙

### 2.1 공통 Model (모든 유저 화면)

| Model 키 | 타입 | 설명 |
|-----------|------|------|
| `isLoggedIn` | `boolean` | 로그인 여부 |
| `isAdmin` | `boolean` | `ROLE_ADMIN` 여부 |
| `currentUser` | `UsersDTO` | 로그인 회원 (nullable) |
| `message` | `String` | 성공 플래시 메시지 (optional) |
| `error` | `String` | 오류 플래시 메시지 (optional) |

### 2.2 Redirect/HTTP Method 관례

| 동작 | Method | 응답 |
|------|--------|------|
| 화면 조회 | `GET` | `200` + HTML |
| 생성/수정/삭제 | `POST` | `302 Redirect` + FlashAttribute |
| 관리자 수정 | `POST` + `_method=put` 또는 `@PutMapping` | 동일 |

### 2.3 View Model (화면 전용, DTO 조합 — 구현 시 추가 권장)

| 클래스명 | 필드 예시 | 사용 화면 |
|----------|-----------|-----------|
| `PersonaListItemVM` | `PersonasDTO` + `PersonaBaseStatsDTO baseStats` | `persona/list` |
| `StorySessionListItemVM` | `id`, `worldName`, `personaName`, `SessionStatus status`, `currentTurn`, `createdAt` | `mypage/sessions` |
| `CurrentStatsVM` | `wisdom`, `strength`, `agility`, `charisma`, `luck` | `story/play` (서버 계산 스탯) |
| `AdminDashboardStatsVM` | `userCount`, `worldCount`, `activeSessionCount`, `pendingReportCount` | `admin/dashboard` |
| `WorldStatsRowVM` | `worldName`, `totalSessions`, `completedSessions`, `abandonedSessions`, `ratingAvg` | `admin/stats` |

---

## 3. Home / 공지

### 3.1 `HomeController`

| # | Method | URL | View (HTML) | 권한 | Input | Output Model | Redirect / 비고 |
|---|--------|-----|-------------|------|-------|----------------|-----------------|
| H-01 | GET | `/` | `home/index` | permitAll | — | `worlds`: `List<WorldsDTO>` (평점순, `isActive=true`)<br>`featuredWorlds`: `List<WorldsDTO>` (인기)<br>`notices`: `List<NoticesDTO>` (활성, 최근 N건)<br>공통 Model | — |
| H-02 | GET | `/worlds` | `world/list` | permitAll | — | `worlds`: `List<WorldsDTO>` | WORLD-01. `/`와 동일 데이터 가능 |

### 3.2 `NoticeViewController`

| # | Method | URL | View | 권한 | Input | Output Model | Redirect / 비고 |
|---|--------|-----|------|------|-------|----------------|-----------------|
| N-01 | GET | `/notices` | `notice/list` | authenticated | — | `notices`: `List<NoticesDTO>` (`isActive=true`) | |
| N-02 | GET | `/notices/{id}` | `notice/detail` | authenticated | Path: `id` Long | `notice`: `NoticesDTO` | 없으면 404 |

---

## 4. 인증 (Auth)

### 4.1 `AuthController` + Spring Security OAuth2

| # | Method | URL | View | 권한 | Input | Output Model | Redirect / 비고 |
|---|--------|-----|------|------|-------|----------------|-----------------|
| A-01 | GET | `/login` | `auth/login` | permitAll | — | 공통 Model | AUTH-01 |
| A-02 | GET | `/oauth2/authorization/{provider}` | — | permitAll | Path: `provider` (`google`, `kakao`, `naver`) | — | Spring Security 처리 -> 콜백 |
| A-03 | GET | `/auth/nickname` | `auth/nickname` | authenticated (닉네임 미설정) | — | `nickname`: `String` (optional)<br>`nicknameAvailable`: `Boolean` (optional) | AUTH-02 |
| A-04 | POST | `/auth/nickname` | — | authenticated | Form: `nickname` String (max 20) | — | 성공 -> `redirect:/`<br>실패 -> `auth/nickname` + `nicknameError` |
| A-05 | GET | `/auth/suspended` | `auth/suspended` | permitAll | — | `suspension`: `UserSuspensionsDTO` (현재 정지) | AUTH-05 |
| A-06 | POST | `/logout` | — | authenticated | — | — | `redirect:/` + 세션/Redis 토큰 제거 (AUTH-04) |

### 4.2 Form / Validation (Auth)

| 파라미터 | 타입 | 필수 | 검증 |
|----------|------|------|------|
| `nickname` | String | Y | 1~20자, 중복 불가 (AUTH-03) |

---

## 5. 세계관 / 페르소나

### 5.1 `WorldViewController`

| # | Method | URL | View | 권한 | Input | Output Model | Redirect / 비고 |
|---|--------|-----|------|------|-------|----------------|-----------------|
| W-01 | GET | `/worlds/{id}` | `world/detail` | permitAll | Path: `id` | `world`: `WorldsDTO` | WORLD-02 |
| W-02 | GET | `/worlds/{id}/personas` | `persona/list` | authenticated | Path: `id` (= worldId) | `world`: `WorldsDTO`<br>`personas`: `List<PersonaListItemVM>` | WORLD-03 -> 페르소나 화면 |

### 5.2 `PersonaViewController`

| # | Method | URL | View | 권한 | Input | Output Model | Redirect / 비고 |
|---|--------|-----|------|------|-------|----------------|-----------------|
| P-01 | POST | `/worlds/{worldId}/personas/{personaId}/sessions` | — | authenticated | Path: `worldId`, `personaId` | — | 세션 생성 후 `redirect:/sessions/{sessionId}/play` (PERSONA-03)<br>Output: `StorySessionsDTO` (DB 저장) |

---

## 6. 스토리 / 세션

### 6.1 `StoryViewController`

| # | Method | URL | View | 권한 | Input | Output Model | Redirect / 비고 |
|---|--------|-----|------|------|-------|----------------|-----------------|
| S-01 | GET | `/sessions/{id}/play` | `story/play` | authenticated (본인 세션) | Path: `id` | `session`: `StorySessionsDTO`<br>`world`: `WorldsDTO`<br>`persona`: `PersonasDTO`<br>`messages`: `List<ChatMessagesDTO>`<br>`items`: `List<SessionItemsDTO>` (`isActive=true`)<br>`currentStats`: `CurrentStatsVM`<br>`progressPercent`: `int`<br>`currentStory`: `String` (optional)<br>`choices`: `List<String>` (optional) | STORY-01~07, INV-01 |
| S-02 | POST | `/sessions/{id}/abandon` | — | authenticated | Path: `id` | — | `status=ABANDONED` -> `redirect:/mypage/sessions` |
| S-03 | GET | `/sessions/{id}/ending` | `story/ending` | authenticated | Path: `id` | `session`, `world`, `persona`, `endingStory`: `String` | ENDING-02 |
| S-04 | POST | `/sessions/{id}/epilogue` | — | authenticated | Path: `id` | — | `status=EPILOGUE` -> `redirect:/sessions/{id}/epilogue` (ENDING-04) |
| S-05 | GET | `/sessions/{id}/epilogue` | `story/epilogue` | authenticated | Path: `id` | `session`, `snapshotStats`: `SessionStatSnapshotsDTO`<br>`epilogueMessages`: `List<ChatMessagesDTO>` | EPILOGUE-01~02 |
| S-06 | POST | `/sessions/{id}/complete` | — | authenticated | Path: `id` | — | `status=COMPLETED` -> `redirect:/worlds/{worldId}/ratings?sessionId=` (평점 UI) |

### 6.2 `StoryViewController` — 인벤토리

| # | Method | URL | View | 권한 | Input | Output Model | Redirect / 비고 |
|---|--------|-----|------|------|-------|----------------|-----------------|
| S-07 | POST | `/sessions/{sessionId}/items/{itemId}/drop` | — | authenticated | Path: `sessionId`, `itemId` | — | `redirect:/sessions/{sessionId}/play` (INV-02) |

> 스토리 입력/선택지 전송은 **WebSocket** ([API_명세서](./API_명세서.md) STORY-WS) 사용. 폼 POST 대체 API도 병행 가능.

---

## 7. 마이페이지

### 7.1 `MypageController`

| # | Method | URL | View | 권한 | Input | Output Model | Redirect / 비고 |
|---|--------|-----|------|------|-------|----------------|-----------------|
| M-01 | GET | `/mypage` | `mypage/index` | authenticated | — | `user`: `UsersDTO`<br>`recentSessions`: `List<StorySessionListItemVM>` | MY-02 요약 |
| M-02 | GET | `/mypage/profile` | `mypage/profile` | authenticated | — | `user`: `UsersDTO` | MY-01 |
| M-03 | POST | `/mypage/profile` | — | authenticated | Form: `nickname` | — | `redirect:/mypage/profile` |
| M-04 | POST | `/mypage/withdraw` | — | authenticated | — | — | `users.status=DELETED` -> `redirect:/logout` (MY-05) |
| M-05 | GET | `/mypage/sessions` | `mypage/sessions` | authenticated | Query: `status` (optional, `SessionStatus`) | `sessions`: `List<StorySessionListItemVM>` | MY-02, MY-03 |
| M-06 | GET | `/mypage/ratings` | `mypage/ratings` | authenticated | — | `ratings`: `List<WorldRatingsDTO>` + `worldName` | MY-04 |

---

## 8. 평점 / 리뷰

### 8.1 `RatingViewController`

| # | Method | URL | View | 권한 | Input | Output Model | Redirect / 비고 |
|---|--------|-----|------|------|-------|----------------|-----------------|
| R-01 | GET | `/worlds/{worldId}/ratings` | `rating/form` | authenticated | Path: `worldId`<br>Query: `sessionId` Long | `world`: `WorldsDTO`<br>`sessionId`: Long<br>`existingRating`: `WorldRatingsDTO` (optional) | RATING-01~02 |
| R-02 | GET | `/worlds/{worldId}/ratings/edit` | `rating/form` | authenticated | Path: `worldId` | R-01 동일 | RATING-03 수정 |
| R-03 | POST | `/worlds/{worldId}/ratings` | — | authenticated | Form: `sessionId`, `score` (1~5), `review` (optional, max 200) | — | 저장/평균 재계산 -> `redirect:/mypage/ratings` (RATING-04) |

---

## 9. 신고

### 9.1 `ReportViewController`

| # | Method | URL | View | 권한 | Input | Output Model | Redirect / 비고 |
|---|--------|-----|------|------|-------|----------------|-----------------|
| RP-01 | GET | `/reports/new` | `report/form` | authenticated | Query: `targetType`, `targetId` | `targetType`: `ReportTargetType`<br>`targetId`: Long | |
| RP-02 | POST | `/reports` | — | authenticated | Form: `targetType`, `targetId`, `reason` | — | `redirect:` 이전 페이지 |

---

## 10. 관리자 (ROLE_ADMIN)

### 10.1 `AdminDashboardController`

| # | Method | URL | View | 권한 | Input | Output Model |
|---|--------|-----|------|------|-------|----------------|
| AD-01 | GET | `/admin` | `admin/dashboard` | ADMIN | — | `stats`: `AdminDashboardStatsVM` |
| AD-02 | GET | `/admin/stats` | `admin/stats/index` | ADMIN | — | `worldStats`: `List<WorldStatsRowVM>` |

### 10.2 `AdminUserController`

| # | Method | URL | View | 권한 | Input | Output Model | Redirect |
|---|--------|-----|------|------|-------|----------------|----------|
| AU-01 | GET | `/admin/users` | `admin/users/list` | ADMIN | Query: `q`, `status` | `users`: `List<UsersDTO>` | |
| AU-02 | GET | `/admin/users/{id}` | `admin/users/detail` | ADMIN | Path: `id` | `user`, `socialAccounts`, `suspensions` | |
| AU-03 | POST | `/admin/users/{id}/suspend` | — | ADMIN | Form: `reason` | — | `redirect:/admin/users/{id}` |
| AU-04 | POST | `/admin/users/{id}/lift` | — | ADMIN | — | — | 동일 |
| AU-05 | POST | `/admin/users/{id}/delete` | — | ADMIN | — | — | `redirect:/admin/users` |

### 10.3 `AdminWorldController`

| # | Method | URL | View | 권한 | Input | Output Model | Redirect |
|---|--------|-----|------|------|-------|----------------|----------|
| AW-01 | GET | `/admin/worlds` | `admin/worlds/list` | ADMIN | — | `worlds`: `List<WorldsDTO>` | |
| AW-02 | GET | `/admin/worlds/new` | `admin/worlds/form` | ADMIN | — | `world`: empty `WorldsDTO` | |
| AW-03 | GET | `/admin/worlds/{id}/edit` | `admin/worlds/form` | ADMIN | Path: `id` | `world`: `WorldsDTO` | |
| AW-04 | POST | `/admin/worlds` | — | ADMIN | Form: `name`, `genre`, `description`, `settingText`, `targetTurn`, `isActive` | — | `redirect:/admin/worlds` |
| AW-05 | PUT/POST | `/admin/worlds/{id}` | — | ADMIN | 동일 Form | — | `redirect:/admin/worlds` |
| AW-06 | POST | `/admin/worlds/{id}/toggle` | — | ADMIN | Path: `id` | — | `isActive` 토글 |

### 10.4 `AdminPersonaController`

| # | Method | URL | View | 권한 | Input | Output Model | Redirect |
|---|--------|-----|------|------|-------|----------------|----------|
| AP-01 | GET | `/admin/personas` | `admin/personas/list` | ADMIN | Query: `worldId` | `personas`, `worlds` | |
| AP-02 | GET | `/admin/personas/new` | `admin/personas/form` | ADMIN | — | `persona`, `baseStats`, `worlds` | |
| AP-03 | GET | `/admin/personas/{id}/edit` | `admin/personas/form` | ADMIN | Path: `id` | 동일 | |
| AP-04 | POST | `/admin/personas` | — | ADMIN | Form: `worldId`, `name`, `description`, `background`, `isActive`, `wisdom`~`luck` | — | `redirect:/admin/personas` |

### 10.5 `AdminItemController`

| # | Method | URL | View | 권한 | Input | Output Model | Redirect |
|---|--------|-----|------|------|-------|----------------|----------|
| AI-01 | GET | `/admin/items/grades` | `admin/items/grades` | ADMIN | — | `gradeConfigs`: `List<ItemGradeConfigsDTO>` | |
| AI-02 | POST | `/admin/items/grades/{id}` | — | ADMIN | Form: `statSumMax`, `singleMax`, `singleMin`, `canNegative` | — | `redirect:/admin/items/grades` |

### 10.6 `AdminSessionController`

| # | Method | URL | View | 권한 | Input | Output Model | Redirect |
|---|--------|-----|------|------|-------|----------------|----------|
| AS-01 | GET | `/admin/sessions` | `admin/sessions/list` | ADMIN | Query: `userId`, `worldId`, `status` | `sessions`: `List<StorySessionListItemVM>`, `worlds` | |
| AS-02 | POST | `/admin/sessions/{id}/abandon` | — | ADMIN | Path: `id` | — | 강제 `ABANDONED` |

### 10.7 `AdminRatingController`

| # | Method | URL | View | 권한 | Input | Output Model | Redirect |
|---|--------|-----|------|------|-------|----------------|----------|
| AR-01 | GET | `/admin/ratings` | `admin/ratings/list` | ADMIN | Query: `nickname`, `worldId` | `ratings` (+ `userNickname`, `worldName`) | |
| AR-02 | DELETE/POST | `/admin/ratings/{id}` | — | ADMIN | Path: `id` | — | 리뷰 삭제 |

### 10.8 `AdminNoticeController`

| # | Method | URL | View | 권한 | Input | Output Model | Redirect |
|---|--------|-----|------|------|-------|----------------|----------|
| AN-01 | GET | `/admin/notices` | `admin/notices/list` | ADMIN | — | `notices`: `List<NoticesDTO>` | |
| AN-02 | GET | `/admin/notices/new` | `admin/notices/form` | ADMIN | — | `notice`: empty | |
| AN-03 | GET | `/admin/notices/{id}/edit` | `admin/notices/form` | ADMIN | Path: `id` | `notice` | |
| AN-04 | POST | `/admin/notices` | — | ADMIN | Form: `title`, `content`, `isActive` | — | `redirect:/admin/notices` |
| AN-05 | DELETE/POST | `/admin/notices/{id}` | — | ADMIN | Path: `id` | — | 삭제 |

---

## 11. 에러 페이지

| # | Method | URL | View | Trigger |
|---|--------|-----|------|---------|
| E-01 | — | — | `error/404` | `NoHandlerFoundException`, 리소스 없음 |
| E-02 | — | — | `error/403` | 접근 거부 (`AccessDeniedException`) |

Spring Boot 설정 예: `server.error.path` / `ErrorController` 커스텀.

---

## 12. 요구사항 ID ↔ Controller 매핑

| 요구사항 ID | Controller 엔드포인트 |
|-------------|----------------------|
| AUTH-01~05 | §4 |
| MY-01~05 | §7 |
| WORLD-01~03 | §3, §5 |
| PERSONA-01~03 | §5.2 |
| STORY-01~07 | §6 + API WebSocket |
| INV-01~02 | §6 |
| ENDING-01~04, EPILOGUE-01~04 | §6 |
| RATING-01~05 | §8 |
| ADMIN-* | §10 |

---

## 13. 변경 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| v1.0 | 2026-05-29 | 템플릿 기준 최초 작성 |
