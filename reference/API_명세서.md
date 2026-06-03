# World Tale Talk — API 명세서

| 항목 | 내용 |
|------|------|
| 버전 | v1.0 |
| Base URL (REST) | `/api/v1` |
| Base URL (Page) | `/` (Controller 명세: [Controller_명세서.md](./Controller_명세서.md)) |
| 인증 | Session Cookie (SSR) / Bearer JWT (선택, REST·WS) |
| Content-Type | `application/json` (REST·WS 메시지) |
| 작성일 | 2026-05-29 |

---

## 1. 개요

| API 종류 | 용도 | 구현 위치 권장 |
|----------|------|----------------|
| **Page API** | Thymeleaf HTML | `@Controller` — [Controller_명세서.md](./Controller_명세서.md) |
| **REST API** | AJAX 검증, 모바일·분리 프론트, 관리 도구 | `@RestController` (`com.worldtaletalk.api`) |
| **WebSocket** | 스토리 실시간 스트리밍 (STORY-04) | `@Controller` + STOMP 또는 Raw WebSocket |
| **Internal** | LLM Structured Output 파싱 | Service 계층 (클라이언트 노출 없음) |

### 1.1 공통 응답 래퍼 (REST 권장)

```json
{
  "success": true,
  "data": { },
  "error": null,
  "timestamp": "2026-05-29T14:00:00Z"
}
```

에러 시:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "NICKNAME_DUPLICATE",
    "message": "이미 사용 중인 닉네임입니다."
  },
  "timestamp": "2026-05-29T14:00:00Z"
}
```

### 1.2 HTTP 상태 코드

| 코드 | 의미 |
|------|------|
| 200 | 성공 (조회·WebSocket 핸드셰이크) |
| 201 | 생성 성공 |
| 400 | Validation 실패 |
| 401 | 미인증 |
| 403 | 권한 없음 (ADMIN 등) |
| 404 | 리소스 없음 |
| 409 | 중복 (닉네임, 평점 등) |
| 422 | 비즈니스 규칙 위반 (정지 계정, 세션 상태 불일치) |
| 500 | 서버 오류 |

---

## 2. 인증 API

| # | Method | URL | 권한 | Input | Output | HTML 페이지 | 비고 |
|---|--------|-----|------|-------|--------|-------------|------|
| API-A-01 | GET | `/oauth2/authorization/{provider}` | permitAll | Path: `provider` | Redirect OAuth | `auth/login` | Spring Security |
| API-A-02 | GET | `/api/v1/auth/me` | authenticated | — | `UsersDTO` | — | 현재 로그인 정보 |
| API-A-03 | GET | `/api/v1/auth/nickname/check` | authenticated | Query: `nickname` | `{ "available": boolean }` | `auth/nickname` | AUTH-03 실시간 중복 검사 |
| API-A-04 | POST | `/api/v1/auth/nickname` | authenticated | Body: `NicknameRequest` | `UsersDTO` | `auth/nickname` | AUTH-02 |
| API-A-05 | POST | `/logout` | authenticated | — | `204` 또는 Redirect | header | AUTH-04 |

### 2.1 `NicknameRequest`

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `nickname` | string | Y | 1~20자 |

---

## 3. 세계관 API

| # | Method | URL | 권한 | Input | Output | HTML 페이지 | 비고 |
|---|--------|-----|------|-------|--------|-------------|------|
| API-W-01 | GET | `/api/v1/worlds` | permitAll | Query: `sort` (`rating`, default) | `List<WorldsDTO>` | `home/index`, `world/list` | `isActive=true`만 |
| API-W-02 | GET | `/api/v1/worlds/featured` | permitAll | Query: `limit` (default 3) | `List<WorldsDTO>` | `home/index` | RATING-05 |
| API-W-03 | GET | `/api/v1/worlds/{id}` | permitAll | Path: `id` | `WorldsDTO` | `world/detail` | WORLD-02 |

### 3.1 `WorldsDTO` (Output)

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | long | PK |
| `name` | string | 세계관 이름 |
| `genre` | enum | `FANTASY`, `SF`, `MARTIAL_ARTS`, `MYSTERY` |
| `description` | string | 카드 소개 (유저용) |
| `settingText` | string | LLM용 (관리자·내부만 노출 권장) |
| `targetTurn` | int | 목표 턴 |
| `isActive` | boolean | 노출 여부 |
| `ratingAvg` | decimal | 평균 평점 |
| `playCount` | int | 플레이 수 |

---

## 4. 페르소나 API

| # | Method | URL | 권한 | Input | Output | HTML 페이지 | 비고 |
|---|--------|-----|------|-------|--------|-------------|------|
| API-P-01 | GET | `/api/v1/worlds/{worldId}/personas` | authenticated | Path: `worldId` | `List<PersonaDetailResponse>` | `persona/list` | PERSONA-01 |
| API-P-02 | POST | `/api/v1/worlds/{worldId}/personas/{personaId}/sessions` | authenticated | Path: `worldId`, `personaId` | `StorySessionsDTO` | — | PERSONA-03, 201 |

### 4.1 `PersonaDetailResponse` (Output)

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | long | 페르소나 PK |
| `worldId` | long | 세계관 FK |
| `name` | string | 이름 |
| `description` | string | 유저용 소개 |
| `background` | string | LLM용 (선택적 마스킹) |
| `isActive` | boolean | |
| `baseStats` | `PersonaBaseStatsDTO` | PERSONA-02 |

### 4.2 `PersonaBaseStatsDTO`

| 필드 | 타입 |
|------|------|
| `wisdom`, `strength`, `agility`, `charisma`, `luck` | int |

---

## 5. 플레이 세션 API

| # | Method | URL | 권한 | Input | Output | HTML 페이지 | 비고 |
|---|--------|-----|------|-------|--------|-------------|------|
| API-S-01 | GET | `/api/v1/sessions/{id}` | authenticated (본인) | Path: `id` | `SessionDetailResponse` | `story/play` | |
| API-S-02 | GET | `/api/v1/sessions/me` | authenticated | Query: `status` | `List<StorySessionListItemResponse>` | `mypage/sessions` | MY-02 |
| API-S-03 | POST | `/api/v1/sessions/{id}/abandon` | authenticated | Path: `id` | `StorySessionsDTO` | — | `ABANDONED` |
| API-S-04 | POST | `/api/v1/sessions/{id}/epilogue` | authenticated | Path: `id` | `StorySessionsDTO` | — | `EPILOGUE` |
| API-S-05 | POST | `/api/v1/sessions/{id}/complete` | authenticated | Path: `id` | `StorySessionsDTO` | — | `COMPLETED` |
| API-S-06 | GET | `/api/v1/sessions/{id}/stats` | authenticated | Path: `id` | `CurrentStatsResponse` | `story/play` | 서버 계산 스탯 |
| API-S-07 | GET | `/api/v1/sessions/{id}/ending` | authenticated | Path: `id` | `EndingResponse` | `story/ending` | |

### 5.1 `StorySessionsDTO` (Output)

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | long | 세션 PK |
| `userId` | long | |
| `worldId` | long | |
| `personaId` | long | |
| `status` | enum | `IN_PROGRESS`, `ENDING`, `EPILOGUE`, `COMPLETED`, `ABANDONED` |
| `currentTurn` | int | |
| `endingType` | enum? | `TRUE`, `GOOD`, `BAD`, `SECRET`, `DARK` |

### 5.2 `SessionDetailResponse` (Output)

| 필드 | 타입 | 설명 |
|------|------|------|
| `session` | `StorySessionsDTO` | |
| `world` | `WorldsDTO` | |
| `persona` | `PersonasDTO` | |
| `messages` | `List<ChatMessagesDTO>` | |
| `items` | `List<SessionItemsDTO>` | `isActive=true` |
| `currentStats` | `CurrentStatsResponse` | |
| `progressPercent` | int | 0~100 |

### 5.3 `CurrentStatsResponse` (Output)

| 필드 | 타입 | 설명 |
|------|------|------|
| `wisdom`, `strength`, `agility`, `charisma`, `luck` | int | 기본+로그+아이템 합산 |

### 5.4 `EndingResponse` (Output)

| 필드 | 타입 |
|------|------|
| `session` | `StorySessionsDTO` |
| `endingType` | enum |
| `endingStory` | string |

---

## 6. 채팅 · 스토리 API (REST 대안)

WebSocket 미지원 클라이언트용. 실서비스는 §7 WebSocket 우선.

| # | Method | URL | 권한 | Input | Output | HTML | 비고 |
|---|--------|-----|------|-------|--------|------|------|
| API-C-01 | POST | `/api/v1/sessions/{id}/turns` | authenticated | Body: `TurnRequest` | `TurnResponse` | `story/play` | 선택지·자유 입력 |
| API-C-02 | GET | `/api/v1/sessions/{id}/messages` | authenticated | Query: `turn`, `page`, `size` | `Page<ChatMessagesDTO>` | — | STORY-05 |
| API-C-03 | POST | `/api/v1/sessions/{id}/choices` | authenticated | Body: `{ "choiceIndex": 0\|1\|2 }` | `TurnResponse` | — | STORY-02 |

### 6.1 `TurnRequest` (Input)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `content` | string | N* | 자유 입력 (STORY-03). `choiceIndex`와 택1 |
| `choiceIndex` | int | N* | 0~2 |

### 6.2 `TurnResponse` (Output)

| 필드 | 타입 | 설명 |
|------|------|------|
| `turn` | int | 현재 턴 |
| `story` | string | 소설 본문 |
| `choices` | string[3] | 선택지 |
| `statChanges` | object | 적용 후 스탯 변화 요약 |
| `itemEvent` | `ItemEventResponse`? | GAIN/LOSE/NULL |
| `endingTriggered` | boolean | 엔딩 진입 여부 |
| `sessionStatus` | enum | |

### 6.3 `ChatMessagesDTO` (Output)

| 필드 | 타입 |
|------|------|
| `id` | long |
| `storySessionId` | long |
| `turn` | int |
| `role` | `USER`, `ASSISTANT` |
| `content` | string |
| `isEpilogue` | boolean |

---

## 7. WebSocket API (STORY-04)

| # | Protocol | URL | 권한 | Input (Client → Server) | Output (Server → Client) | HTML |
|---|----------|-----|------|-------------------------|--------------------------|------|
| WS-01 | WebSocket | `/ws/story/{sessionId}` | authenticated | `StoryInputMessage` | `StoryStreamMessage` (스트리밍) | `story/play` |
| WS-02 | WebSocket | `/ws/epilogue/{sessionId}` | authenticated | `EpilogueInputMessage` | `StoryStreamMessage` | `story/epilogue` |

### 7.1 `StoryInputMessage` (Input)

```json
{
  "type": "CHOICE",
  "choiceIndex": 1
}
```

```json
{
  "type": "FREE_TEXT",
  "content": "문을 부수고 들어간다."
}
```

| 필드 | 타입 | 필수 |
|------|------|------|
| `type` | `CHOICE` \| `FREE_TEXT` | Y |
| `choiceIndex` | int | `CHOICE`일 때 |
| `content` | string | `FREE_TEXT`일 때 |

### 7.2 `StoryStreamMessage` (Output)

스트리밍 시 청크 예:

```json
{ "event": "DELTA", "field": "story", "value": "어둠 속에서..." }
```

완료 시:

```json
{
  "event": "COMPLETE",
  "turn": 5,
  "story": "전체 본문",
  "choices": ["선택1", "선택2", "선택3"],
  "statChanges": { "wisdom": 1, "strength": 0 },
  "itemEvent": {
    "type": "GAIN",
    "itemName": "녹슨 열쇠",
    "itemDescription": "...",
    "itemContext": "NORMAL"
  },
  "endingFlag": false,
  "progressPercent": 25
}
```

---

## 8. 인벤토리 API

| # | Method | URL | 권한 | Input | Output | HTML | 비고 |
|---|--------|-----|------|-------|--------|------|------|
| API-I-01 | GET | `/api/v1/sessions/{id}/items` | authenticated | Path: `id` | `List<SessionItemsDTO>` | play sidebar | INV-01 |
| API-I-02 | POST | `/api/v1/sessions/{sessionId}/items/{itemId}/drop` | authenticated | Path IDs | `SessionItemsDTO` | — | INV-02, `isActive=false` |

### 8.1 `SessionItemsDTO` (Output)

| 필드 | 타입 |
|------|------|
| `id` | long |
| `storySessionId` | long |
| `itemName`, `itemDescription` | string |
| `grade` | `COMMON`, `RARE`, `EPIC`, `CURSED` |
| `wisdomBonus` ~ `luckBonus` | int |
| `isActive` | boolean |
| `gainedAtTurn` | int |
| `droppedAtTurn` | int? |

---

## 9. 평점 · 리뷰 API

| # | Method | URL | 권한 | Input | Output | HTML | 비고 |
|---|--------|-----|------|-------|--------|------|------|
| API-R-01 | GET | `/api/v1/worlds/{worldId}/ratings/me` | authenticated | Path: `worldId` | `WorldRatingsDTO` | `rating/form` | RATING-03 |
| API-R-02 | PUT | `/api/v1/worlds/{worldId}/ratings` | authenticated | Body: `RatingRequest` | `WorldRatingsDTO` | — | 생성·수정 |
| API-R-03 | GET | `/api/v1/users/me/ratings` | authenticated | — | `List<WorldRatingListResponse>` | `mypage/ratings` | MY-04 |

### 9.1 `RatingRequest` (Input)

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `sessionId` | long | Y | 플레이 완료 세션 |
| `score` | short | Y | 1~5 |
| `review` | string | N | max 200 |

### 9.2 `WorldRatingsDTO` (Output)

| 필드 | 타입 |
|------|------|
| `id`, `userId`, `worldId`, `storySessionId` | long |
| `score` | short (1~5) |
| `review` | string? |

---

## 10. 공지 API

| # | Method | URL | 권한 | Input | Output | HTML |
|---|--------|-----|------|-------|--------|------|
| API-N-01 | GET | `/api/v1/notices` | authenticated | Query: `active=true` | `List<NoticesDTO>` | `notice/list` |
| API-N-02 | GET | `/api/v1/notices/{id}` | authenticated | Path: `id` | `NoticesDTO` | `notice/detail` |

### 10.1 `NoticesDTO`

| 필드 | 타입 |
|------|------|
| `id`, `adminId` | long |
| `title` | string (max 100) |
| `content` | string |
| `isActive` | boolean |

---

## 11. 신고 API

| # | Method | URL | 권한 | Input | Output | HTML |
|---|--------|-----|------|-------|--------|------|
| API-RP-01 | POST | `/api/v1/reports` | authenticated | Body: `ReportRequest` | `ReportsDTO` | `report/form` |

### 11.1 `ReportRequest` (Input)

| 필드 | 타입 | 필수 |
|------|------|------|
| `targetType` | `REVIEW`, `CHAT_MESSAGE` | Y |
| `targetId` | long | Y |
| `reason` | string | Y |

### 11.2 `ReportsDTO` (Output)

| 필드 | 타입 |
|------|------|
| `id` | long |
| `targetType`, `targetId` | |
| `reason` | string |
| `status` | `PENDING`, `RESOLVED`, `DISMISSED` |

---

## 12. 관리자 REST API

Base: `/api/v1/admin` — 전역 `@PreAuthorize("hasRole('ADMIN')")`

| # | Method | URL | Input | Output | HTML |
|---|--------|-----|-------|--------|------|
| API-AD-01 | GET | `/dashboard` | — | `AdminDashboardStatsResponse` | `admin/dashboard` |
| API-AD-02 | GET | `/users` | Query: `q`, `status`, `page` | `Page<UsersDTO>` | `admin/users/list` |
| API-AD-03 | GET | `/users/{id}` | Path: `id` | `UserDetailResponse` | `admin/users/detail` |
| API-AD-04 | POST | `/users/{id}/suspend` | Body: `{ "reason": "..." }` | `UserSuspensionsDTO` | — |
| API-AD-05 | POST | `/users/{id}/lift` | — | `UserSuspensionsDTO` | — |
| API-AD-06 | CRUD | `/worlds` | `WorldUpsertRequest` | `WorldsDTO` | `admin/worlds/*` |
| API-AD-07 | CRUD | `/personas` | `PersonaUpsertRequest` | `PersonasDTO` | `admin/personas/*` |
| API-AD-08 | GET/PUT | `/items/grades` | `ItemGradeConfigUpdate` | `ItemGradeConfigsDTO` | `admin/items/grades` |
| API-AD-09 | GET/PUT | `/items/probabilities` | `probability` | `ItemGradeProbabilities` | `admin/items/probabilities` |
| API-AD-10 | GET | `/sessions` | Query filters | `Page<StorySessionListItemResponse>` | `admin/sessions/list` |
| API-AD-11 | POST | `/sessions/{id}/abandon` | — | `StorySessionsDTO` | — |
| API-AD-12 | GET/DELETE | `/ratings` | filters | `Page<WorldRatingAdminResponse>` | `admin/ratings/list` |
| API-AD-13 | CRUD | `/notices` | `NoticeUpsertRequest` | `NoticesDTO` | `admin/notices/*` |
| API-AD-14 | GET | `/stats/worlds` | — | `List<WorldStatsRowResponse>` | `admin/stats` |

---

## 13. LLM Internal API (서버 내부, STORY·ITEM·ENDING)

클라이언트 직접 호출 없음. Spring AI `ChatClient` 응답 파싱용.

### 13.1 LLM Structured Output — `LlmTurnResult` (Output)

요구사항 정의서 §4.2 기준.

| 필드 | 타입 | 설명 |
|------|------|------|
| `story` | string | 소설 본문 (STORY-01) |
| `choices` | string[3] | 선택지 (STORY-02) |
| `statChanges` | Map<String, Integer> | 키: `wisdom`,`strength`,`agility`,`charisma`,`luck` — 서버에서 -2~+2 클램핑 (STAT-01) |
| `itemEvent` | `LlmItemEvent`? | |
| `endingFlag` | boolean | 엔딩 조건 검사 트리거 (ENDING-01) |

### 13.2 `LlmItemEvent`

| 필드 | 타입 | 설명 |
|------|------|------|
| `type` | `GAIN` \| `LOSE` \| `NULL` | |
| `itemName` | string | LLM 생성 |
| `itemDescription` | string | LLM 생성 |
| `itemContext` | `MINOR` \| `NORMAL` \| `SIGNIFICANT` \| `LEGENDARY` | 등급 힌트 → 서버 등급 결정 (ITEM-02) |

### 13.3 서버 후처리 Output — `TurnProcessResult`

| 필드 | 타입 | 설명 |
|------|------|------|
| `session` | `StorySessionsDTO` | 턴·상태 갱신 |
| `appliedStatLog` | `SessionStatLogsDTO` | STAT-02 |
| `createdItem` | `SessionItemsDTO`? | ITEM-04 |
| `removedItemId` | long? | INV-04 |
| `endingType` | enum? | 서버 판별 (ENDING-01) |
| `snapshot` | `SessionStatSnapshotsDTO`? | 엔딩 시 (STAT-04) |

---

## 14. Page URL ↔ API ↔ View 매핑 요약

| HTML (View) | Page URL (GET) | 대응 REST (선택) |
|-------------|----------------|------------------|
| `home/index` | `/` | `GET /api/v1/worlds`, `/featured` |
| `auth/login` | `/login` | OAuth |
| `auth/nickname` | `/auth/nickname` | `GET/POST .../auth/nickname` |
| `world/detail` | `/worlds/{id}` | `GET /api/v1/worlds/{id}` |
| `persona/list` | `/worlds/{id}/personas` | `GET .../personas` |
| `story/play` | `/sessions/{id}/play` | `GET .../sessions/{id}`, WS `/ws/story/{id}` |
| `story/ending` | `/sessions/{id}/ending` | `GET .../ending` |
| `story/epilogue` | `/sessions/{id}/epilogue` | WS `/ws/epilogue/{id}` |
| `mypage/*` | `/mypage/**` | `GET /api/v1/users/me/*` |
| `rating/form` | `/worlds/{id}/ratings` | `PUT .../ratings` |
| `admin/**` | `/admin/**` | `GET/POST /api/v1/admin/**` |

---

## 15. Enum 참조 (DB 설계서 기준)

| Enum | 값 |
|------|-----|
| `UserRole` | USER, ADMIN |
| `UserStatus` | ACTIVE, SUSPENDED, DELETED |
| `SocialProvider` | GOOGLE, KAKAO, NAVER |
| `WorldGenre` | FANTASY, SF, MARTIAL_ARTS, MYSTERY |
| `SessionStatus` | IN_PROGRESS, ENDING, EPILOGUE, COMPLETED, ABANDONED |
| `EndingType` | TRUE, GOOD, BAD, SECRET, DARK |
| `ItemGrade` | COMMON, RARE, EPIC, CURSED |
| `PhaseType` | EARLY, MID, LATE |
| `ChatRole` | USER, ASSISTANT |
| `ReportTargetType` | REVIEW, CHAT_MESSAGE |
| `ReportStatus` | PENDING, RESOLVED, DISMISSED |

---

## 16. 변경 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| v1.0 | 2026-05-29 | 템플릿·DTO·요구사항 기준 최초 작성 |
