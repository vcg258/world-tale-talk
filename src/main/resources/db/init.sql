-- DB 생성
CREATE DATABASE "world_tale_talk";

-- EXTENSION VECTOR 설치
CREATE EXTENSION IF NOT EXISTS vector;

-- ENUM 타입 선언

-- users.role
DROP TYPE IF EXISTS user_role CASCADE;
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');

-- users.status
DROP TYPE IF EXISTS user_status CASCADE;
CREATE TYPE user_status AS ENUM ('ACTIVE', 'SUSPENDED', 'DELETED');

-- social_accounts.provider
DROP TYPE IF EXISTS social_provider CASCADE;
CREATE TYPE social_provider AS ENUM ('GOOGLE', 'KAKAO', 'NAVER');

-- worlds.genre
DROP TYPE IF EXISTS world_genre CASCADE;
CREATE TYPE world_genre AS ENUM ('FANTASY', 'SF', 'MARTIAL_ARTS', 'MYSTERY');

-- item_grade_configs.grade / item_grade_probabilities.grade / session_items.grade
DROP TYPE IF EXISTS item_grade CASCADE;
CREATE TYPE item_grade AS ENUM ('COMMON', 'RARE', 'EPIC', 'CURSED');

-- item_grade_probabilities.phase
DROP TYPE IF EXISTS phase_type CASCADE;
CREATE TYPE phase_type AS ENUM ('EARLY', 'MID', 'LATE');

-- story_sessions.status
DROP TYPE IF EXISTS session_status CASCADE;
CREATE TYPE session_status AS ENUM ('IN_PROGRESS', 'ENDING', 'EPILOGUE', 'COMPLETED', 'ABANDONED');

-- story_sessions.ending_type
DROP TYPE IF EXISTS ending_type CASCADE;
CREATE TYPE ending_type AS ENUM ('TRUE', 'GOOD', 'BAD', 'SECRET', 'DARK');

-- chat_messages.role
DROP TYPE IF EXISTS chat_role CASCADE;
CREATE TYPE chat_role AS ENUM ('USER', 'ASSISTANT');

-- reports.target_type
DROP TYPE IF EXISTS report_target_type CASCADE;
CREATE TYPE report_target_type AS ENUM ('REVIEW', 'CHAT_MESSAGE');

-- reports.status
DROP TYPE IF EXISTS report_status CASCADE;
CREATE TYPE report_status AS ENUM ('PENDING', 'RESOLVED', 'DISMISSED');


-- 회원
-- 소셜 로그인 전용 서비스이므로 password 는 NULL 허용
CREATE TABLE IF NOT EXISTS users
(
    id         BIGSERIAL PRIMARY KEY,
    nickname   VARCHAR(20) NOT NULL UNIQUE,
    email      VARCHAR(50) NOT NULL UNIQUE,
    password   VARCHAR(60),                           -- BCrypt 60자 고정, 소셜 전용이므로 NULL 허용
    role       user_role NOT NULL DEFAULT 'USER',   -- USER | ADMIN
    status     user_status NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | SUSPENDED | DELETED
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  users            IS '회원';
COMMENT ON COLUMN users.nickname   IS '회원 닉네임';
COMMENT ON COLUMN users.email      IS '회원 이메일';
COMMENT ON COLUMN users.password   IS '회원 비밀번호. BCrypt 60자. 소셜 전용이면 NULL';
COMMENT ON COLUMN users.role       IS '회원 권한. USER, ADMIN';
COMMENT ON COLUMN users.status     IS '계정 상태. ACTIVE, SUSPENDED, DELETED';
COMMENT ON COLUMN users.created_at IS '가입 일시';
COMMENT ON COLUMN users.updated_at IS '수정 일시';

-- 한 계정에 여러 소셜 provider 연동 가능 (Google + Kakao 동시 연동 허용)
-- provider + provider_id 이중 UNIQUE : 소셜사마다 고유 ID 체계가 달라
-- 우연히 같은 숫자의 ID 가 다른 provider 에서 발급될 수 있으므로 조합으로 중복 방지
CREATE TABLE IF NOT EXISTS social_accounts
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    provider    social_provider  NOT NULL, -- GOOGLE | KAKAO | NAVER
    provider_id VARCHAR(100) NOT NULL, -- 소셜 provider 측 고유 식별자
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_social_accounts_user FOREIGN KEY (user_id) REFERENCES users (id),
    UNIQUE (provider, provider_id)
);

CREATE INDEX IF NOT EXISTS idx_social_accounts_user_id ON social_accounts (user_id);

COMMENT ON TABLE  social_accounts              IS '소셜 로그인 연동 정보';
COMMENT ON COLUMN social_accounts.user_id      IS '회원 아이디';
COMMENT ON COLUMN social_accounts.provider     IS '소셜 로그인 제공사';
COMMENT ON COLUMN social_accounts.provider_id  IS '소셜 제공사 측 고유 식별자';
COMMENT ON COLUMN social_accounts.created_at   IS '연동 일시';


-- 계정 정지 이력
-- lifted_at 이 NULL 이면 현재 정지 중을 의미
CREATE TABLE IF NOT EXISTS user_suspensions
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT    NOT NULL,
    admin_id     BIGINT    NOT NULL,
    reason       TEXT      NOT NULL,               -- 정지 사유
    suspended_at TIMESTAMP NOT NULL DEFAULT NOW(), -- 정지 처리 일시
    lifted_at    TIMESTAMP,                        -- 정지 해제 일시 (NULL 이면 정지 중)
    CONSTRAINT fk_user_suspensions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_suspensions_admin FOREIGN KEY (admin_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_user_suspensions_user_id ON user_suspensions (user_id);

COMMENT ON TABLE  user_suspensions              IS '계정 정지 이력';
COMMENT ON COLUMN user_suspensions.user_id      IS '정지 대상 회원 아이디';
COMMENT ON COLUMN user_suspensions.admin_id     IS '정지 처리한 관리자 아이디';
COMMENT ON COLUMN user_suspensions.reason       IS '정지 사유';
COMMENT ON COLUMN user_suspensions.suspended_at IS '정지 처리 일시';
COMMENT ON COLUMN user_suspensions.lifted_at    IS '정지 해제 일시. NULL 이면 현재 정지 중';


-- 공지사항
-- is_active = false 이면 유저 화면에 노출되지 않음
-- 관리자가 등록 / 수정 / 삭제 가능
CREATE TABLE IF NOT EXISTS notices
(
    id         BIGSERIAL PRIMARY KEY,
    admin_id   BIGINT       NOT NULL,
    title      VARCHAR(50) NOT NULL,              -- 공지 제목
    content    TEXT         NOT NULL,              -- 공지 본문
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE, -- 유저 화면 노출 여부
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notices_admin FOREIGN KEY (admin_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_notices_is_active ON notices (is_active);

COMMENT ON TABLE  notices           IS '공지사항';
COMMENT ON COLUMN notices.admin_id  IS '작성한 관리자 아이디';
COMMENT ON COLUMN notices.title     IS '공지 제목';
COMMENT ON COLUMN notices.content   IS '공지 본문';
COMMENT ON COLUMN notices.is_active IS '유저 화면 노출 여부. false 이면 노출 안 됨';
COMMENT ON COLUMN notices.created_at IS '등록 일시';
COMMENT ON COLUMN notices.updated_at IS '수정 일시';


-- 세계관
-- setting_text 는 LLM System Prompt 에 직접 주입되는 세계관 설정집
-- target_turn 은 진행도(%) 계산 기준값 (현재 턴 / target_turn * 100)
CREATE TABLE IF NOT EXISTS worlds
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(50)   NOT NULL,              -- 세계관 이름
    genre        world_genre   NOT NULL,              -- 장르 : FANTASY | SF | MARTIAL_ARTS | MYSTERY
    description  TEXT  NOT NULL,              -- 유저에게 보이는 카드 소개 텍스트
    setting_text TEXT          NOT NULL,              -- LLM System Prompt 주입용 세계관 설정집
    target_turn  INT           NOT NULL DEFAULT 20,   -- 메인 스토리 목표 턴 수
    is_active    BOOLEAN       NOT NULL DEFAULT TRUE, -- 메인 화면 노출 여부 (관리자 제어)
    rating_avg   NUMERIC(3, 2) NOT NULL DEFAULT 0.00, -- 평균 평점 (평점 저장 시 재계산)
    play_count   INT           NOT NULL DEFAULT 0,    -- 전체 플레이 수 (세션 생성 시 증가)
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_worlds_is_active ON worlds (is_active);
CREATE INDEX IF NOT EXISTS idx_worlds_rating_avg ON worlds (rating_avg DESC); -- 평점 랭킹 정렬용

COMMENT ON TABLE  worlds              IS '세계관';
COMMENT ON COLUMN worlds.name         IS '세계관 이름';
COMMENT ON COLUMN worlds.genre        IS '세계관 장르';
COMMENT ON COLUMN worlds.description  IS '유저에게 보이는 카드 소개 텍스트';
COMMENT ON COLUMN worlds.setting_text IS 'LLM System Prompt 에 주입되는 세계관 설정집';
COMMENT ON COLUMN worlds.target_turn  IS '메인 스토리 목표 턴 수. 진행도 계산 기준값';
COMMENT ON COLUMN worlds.is_active    IS '메인 화면 노출 여부. 관리자 제어';
COMMENT ON COLUMN worlds.rating_avg   IS '평균 평점. 평점 저장 시 재계산';
COMMENT ON COLUMN worlds.play_count   IS '전체 플레이 수. 세션 생성 시 증가';
COMMENT ON COLUMN worlds.created_at   IS '등록 일시';
COMMENT ON COLUMN worlds.updated_at   IS '수정 일시';


-- 페르소나
CREATE TABLE IF NOT EXISTS personas
(
    id          BIGSERIAL PRIMARY KEY,
    world_id    BIGINT       NOT NULL,
    name        VARCHAR(50)  NOT NULL,              -- 페르소나 이름
    description TEXT NOT NULL,              -- 유저에게 보이는 캐릭터 소개
    background  TEXT         NOT NULL,              -- LLM System Prompt 주입용 캐릭터 배경
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE, -- 유저 선택 화면 노출 여부
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_personas_world FOREIGN KEY (world_id) REFERENCES worlds (id)
);

CREATE INDEX IF NOT EXISTS idx_personas_world_id ON personas (world_id);

COMMENT ON TABLE  personas             IS '페르소나(등장인물)';
COMMENT ON COLUMN personas.world_id    IS '소속 세계관 아이디';
COMMENT ON COLUMN personas.name        IS '페르소나 이름';
COMMENT ON COLUMN personas.description IS '유저에게 보이는 캐릭터 소개';
COMMENT ON COLUMN personas.background  IS 'LLM System Prompt 에 주입되는 캐릭터 배경';
COMMENT ON COLUMN personas.is_active   IS '유저 선택 화면 노출 여부';
COMMENT ON COLUMN personas.created_at  IS '등록 일시';
COMMENT ON COLUMN personas.updated_at  IS '수정 일시';


-- 페르소나 기본 스탯 (정적) 1 : 1 관계
CREATE TABLE IF NOT EXISTS persona_base_stats
(
    id         BIGSERIAL PRIMARY KEY,
    persona_id BIGINT NOT NULL UNIQUE,
    wisdom     INT    NOT NULL DEFAULT 5, -- 지혜 초기값
    strength   INT    NOT NULL DEFAULT 5, -- 힘 초기값
    agility    INT    NOT NULL DEFAULT 5, -- 민첩 초기값
    charisma   INT    NOT NULL DEFAULT 5, -- 카리스마 초기값
    luck       INT    NOT NULL DEFAULT 5, -- 행운 초기값
    CONSTRAINT fk_persona_base_stats_persona FOREIGN KEY (persona_id) REFERENCES personas (id)
);

COMMENT ON TABLE  persona_base_stats             IS '페르소나 기본 스탯. personas 와 1:1. 절대 변하지 않는 고정 초기값';
COMMENT ON COLUMN persona_base_stats.persona_id  IS '페르소나 아이디';
COMMENT ON COLUMN persona_base_stats.wisdom      IS '지혜 초기값';
COMMENT ON COLUMN persona_base_stats.strength    IS '힘 초기값';
COMMENT ON COLUMN persona_base_stats.agility     IS '민첩 초기값';
COMMENT ON COLUMN persona_base_stats.charisma    IS '카리스마 초기값';
COMMENT ON COLUMN persona_base_stats.luck        IS '행운 초기값';


-- 아이템 등급별 스탯 보정 기준 (관리자)
CREATE TABLE IF NOT EXISTS item_grade_configs
(
    id           BIGSERIAL PRIMARY KEY,
    grade        item_grade NOT NULL UNIQUE,        --  아이템 등급 : COMMON | RARE | EPIC | CURSED
    stat_sum_max INT         NOT NULL,               -- 스탯 보정 합산 최대치
    single_max   INT         NOT NULL,               -- 단일 스탯 보정 최대치
    single_min   INT         NOT NULL,               -- 단일 스탯 보정 최소치 (CURSED 는 음수)
    can_negative BOOLEAN     NOT NULL DEFAULT FALSE, -- 음수 보정 허용 여부 (TRUE일 경우 음수 허용)
    updated_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  item_grade_configs              IS '아이템 등급별 스탯 보정 기준. 관리자 수정 가능';
COMMENT ON COLUMN item_grade_configs.grade        IS '아이템 등급';
COMMENT ON COLUMN item_grade_configs.stat_sum_max IS '스탯 보정 합산 최대치';
COMMENT ON COLUMN item_grade_configs.single_max   IS '단일 스탯 보정 최대치';
COMMENT ON COLUMN item_grade_configs.single_min   IS '단일 스탯 보정 최소치. CURSED 는 음수';
COMMENT ON COLUMN item_grade_configs.can_negative IS '음수 보정 허용 여부';
COMMENT ON COLUMN item_grade_configs.updated_at   IS '수정 일시';

-- phase + grade 조합 UNIQUE, 동일 구간 내 전체 확률 합산은 100 이어야 함
CREATE TABLE IF NOT EXISTS item_grade_probabilities
(
    id          BIGSERIAL PRIMARY KEY,
    phase       phase_type NOT NULL, -- 진행도 구간 : EARLY | MID | LATE
    grade       item_grade NOT NULL, -- 아이템 등급 : COMMON | RARE | EPIC | CURSED
    probability INT         NOT NULL CHECK (probability BETWEEN 0 AND 100),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (phase, grade)
);

COMMENT ON TABLE  item_grade_probabilities              IS '진행도 구간별 아이템 등급 확률. 관리자 수정 가능';
COMMENT ON COLUMN item_grade_probabilities.phase       IS '진행도 구간. EARLY, MID, LATE';
COMMENT ON COLUMN item_grade_probabilities.grade       IS '아이템 등급';
COMMENT ON COLUMN item_grade_probabilities.probability IS '해당 등급 확률. 동일 구간 합산 100';
COMMENT ON COLUMN item_grade_probabilities.updated_at  IS '수정 일시';


-- 플레이 세션
-- 유저가 세계관 + 페르소나를 선택하면 세션 1개 생성
-- 중도 포기 또는 관리자 강제 종료 시 ABANDONED
CREATE TABLE IF NOT EXISTS story_sessions
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT      NOT NULL,
    world_id     BIGINT      NOT NULL,
    persona_id   BIGINT      NOT NULL,
    status session_status       NOT NULL DEFAULT 'IN_PROGRESS', -- 세션 진행도 : IN_PROGRESS | ENDING | EPILOGUE | COMPLETED | ABANDONED
    current_turn INT         NOT NULL DEFAULT 0,             -- 현재 턴 수 (진행도 계산에 사용)
    ending_type  ending_type,                                -- 조건에 따른 엔딩구분 : TRUE | GOOD | BAD | SECRET | DARK (완료 시 기록)
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_story_sessions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_story_sessions_world FOREIGN KEY (world_id) REFERENCES worlds (id),
    CONSTRAINT fk_story_sessions_persona FOREIGN KEY (persona_id) REFERENCES personas (id)
);

CREATE INDEX IF NOT EXISTS idx_story_sessions_user_id ON story_sessions (user_id);
CREATE INDEX IF NOT EXISTS idx_story_sessions_world_id ON story_sessions (world_id);
CREATE INDEX IF NOT EXISTS idx_story_sessions_status ON story_sessions (status);

COMMENT ON TABLE  story_sessions              IS '플레이 세션';
COMMENT ON COLUMN story_sessions.user_id      IS '플레이 유저 아이디';
COMMENT ON COLUMN story_sessions.world_id     IS '선택한 세계관 아이디';
COMMENT ON COLUMN story_sessions.persona_id   IS '선택한 페르소나 아이디';
COMMENT ON COLUMN story_sessions.status       IS '세션 상태. IN_PROGRESS, ENDING, EPILOGUE, COMPLETED, ABANDONED';
COMMENT ON COLUMN story_sessions.current_turn IS '현재 턴 수';
COMMENT ON COLUMN story_sessions.ending_type  IS '도달한 엔딩 유형. 엔딩 판별 전까지 NULL';
COMMENT ON COLUMN story_sessions.created_at   IS '세션 생성 일시';
COMMENT ON COLUMN story_sessions.updated_at   IS '세션 수정 일시';


-- 엔딩 시점 스탯 스냅샷
-- 엔딩 판별 시점에 현재 스탯을 INSERT 하여 고정
-- 에필로그 진행 중에는 스탯 변화 없이 이 테이블의 값을 참조
-- story_sessions 와 1:1 관계
CREATE TABLE IF NOT EXISTS session_stat_snapshots
(
    id         BIGSERIAL PRIMARY KEY,
    session_id BIGINT    NOT NULL UNIQUE,
    wisdom     INT       NOT NULL, -- 엔딩 시점 지혜
    strength   INT       NOT NULL, -- 엔딩 시점 힘
    agility    INT       NOT NULL, -- 엔딩 시점 민첩
    charisma   INT       NOT NULL, -- 엔딩 시점 카리스마
    luck       INT       NOT NULL, -- 엔딩 시점 행운
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_session_stat_snapshots_session FOREIGN KEY (session_id) REFERENCES story_sessions (id)
);

COMMENT ON TABLE  session_stat_snapshots             IS '엔딩 시점 스탯 스냅샷. story_sessions 와 1:1';
COMMENT ON COLUMN session_stat_snapshots.session_id  IS '플레이 세션 아이디';
COMMENT ON COLUMN session_stat_snapshots.wisdom      IS '엔딩 시점 지혜 확정값';
COMMENT ON COLUMN session_stat_snapshots.strength    IS '엔딩 시점 힘 확정값';
COMMENT ON COLUMN session_stat_snapshots.agility     IS '엔딩 시점 민첩 확정값';
COMMENT ON COLUMN session_stat_snapshots.charisma    IS '엔딩 시점 카리스마 확정값';
COMMENT ON COLUMN session_stat_snapshots.luck        IS '엔딩 시점 행운 확정값';
COMMENT ON COLUMN session_stat_snapshots.created_at  IS '스냅샷 생성 일시';


-- 세션 스탯 변화 이력
-- 선택 또는 이벤트마다 delta 값을 INSERT 하여 누적
-- 실제 스탯 = persona_base_stats + SUM(delta) + SUM(아이템 보정)
-- 서버에서 단일 delta 값은 -2 ~ +2 범위로 클램핑하여 저장
CREATE TABLE IF NOT EXISTS session_stat_logs
(
    id             BIGSERIAL PRIMARY KEY,
    session_id     BIGINT    NOT NULL,
    turn           INT       NOT NULL,           -- 변화가 발생한 턴 번호
    wisdom_delta   INT       NOT NULL DEFAULT 0, -- 지혜 변화량
    strength_delta INT       NOT NULL DEFAULT 0, -- 힘 변화량
    agility_delta  INT       NOT NULL DEFAULT 0, -- 민첩 변화량
    charisma_delta INT       NOT NULL DEFAULT 0, -- 카리스마 변화량
    luck_delta     INT       NOT NULL DEFAULT 0, -- 행운 변화량
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_session_stat_logs_session FOREIGN KEY (session_id) REFERENCES story_sessions (id)
);

CREATE INDEX IF NOT EXISTS idx_session_stat_logs_session_id ON session_stat_logs (session_id);

COMMENT ON TABLE  session_stat_logs              IS '세션 스탯 변화 이력';
COMMENT ON COLUMN session_stat_logs.session_id   IS '플레이 세션 아이디';
COMMENT ON COLUMN session_stat_logs.turn         IS '스탯 변화가 발생한 턴 번호';
COMMENT ON COLUMN session_stat_logs.wisdom_delta   IS '지혜 변화량';
COMMENT ON COLUMN session_stat_logs.strength_delta IS '힘 변화량';
COMMENT ON COLUMN session_stat_logs.agility_delta  IS '민첩 변화량';
COMMENT ON COLUMN session_stat_logs.charisma_delta IS '카리스마 변화량';
COMMENT ON COLUMN session_stat_logs.luck_delta     IS '행운 변화량';
COMMENT ON COLUMN session_stat_logs.created_at     IS '변화 발생 일시';


-- 채팅 메시지
-- is_epilogue = true : 에필로그 구간 메시지 (ChatMemory 복원 시 false 메시지만 복원)
CREATE TABLE IF NOT EXISTS chat_messages
(
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT      NOT NULL,
    turn        INT         NOT NULL,               -- 해당 메시지가 속한 턴 번호
    role      chat_role   NOT NULL,               -- 채팅 대상 : USER | ASSISTANT
    content     TEXT        NOT NULL,               -- 메시지 본문
    is_epilogue BOOLEAN     NOT NULL DEFAULT FALSE, -- 에필로그 구간 여부 (TRUE = 에필로그 대화내용, FALSE = 메인 스토리 대화내용)
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) REFERENCES story_sessions (id)
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id ON chat_messages (session_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_turn ON chat_messages (session_id, turn); -- 이어하기 복원용

COMMENT ON TABLE  chat_messages              IS '채팅 메시지';
COMMENT ON COLUMN chat_messages.session_id   IS '플레이 세션 아이디';
COMMENT ON COLUMN chat_messages.turn         IS '해당 메시지가 속한 턴 번호';
COMMENT ON COLUMN chat_messages.role         IS '발화 주체. USER, ASSISTANT';
COMMENT ON COLUMN chat_messages.content      IS '메시지 본문';
COMMENT ON COLUMN chat_messages.is_epilogue  IS '에필로그 구간 여부. ChatMemory 복원 시 false 메시지만 복원';
COMMENT ON COLUMN chat_messages.created_at   IS '메시지 생성 일시';

-- 세션 인벤토리 (획득 아이템)
-- 아이템 이름과 설명은 LLM 이 자유 생성
-- 등급과 스탯 보정 수치는 서버가 item_grade_configs 기준으로 부여
-- is_active = false : 버린 아이템 (스탯 보정 계산에서 제외)
-- dropped_at_turn 이 NULL 이면 현재 보유 중
CREATE TABLE IF NOT EXISTS session_items
(
    id               BIGSERIAL PRIMARY KEY,
    session_id       BIGINT       NOT NULL,
    item_name        VARCHAR(50) NOT NULL,              -- LLM 생성 아이템 이름
    item_description TEXT NOT NULL,              -- LLM 생성 아이템 설명
    grade            item_grade  NOT NULL,              -- 아이템 등급 : COMMON | RARE | EPIC | CURSED
    wisdom_bonus     INT          NOT NULL DEFAULT 0,    -- 지혜 보정치
    strength_bonus   INT          NOT NULL DEFAULT 0,    -- 힘 보정치
    agility_bonus    INT          NOT NULL DEFAULT 0,    -- 민첩 보정치
    charisma_bonus   INT          NOT NULL DEFAULT 0,    -- 카리스마 보정치
    luck_bonus       INT          NOT NULL DEFAULT 0,    -- 행운 보정치
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE, -- FALSE 면 버린 아이템
    gained_at_turn   INT          NOT NULL,              -- 획득한 턴 번호
    dropped_at_turn  INT,                                -- 버린 턴 번호 (NULL 이면 보유 중)
    CONSTRAINT fk_session_items_session FOREIGN KEY (session_id) REFERENCES story_sessions (id)
);

CREATE INDEX IF NOT EXISTS idx_session_items_session_id ON session_items (session_id);
CREATE INDEX IF NOT EXISTS idx_session_items_session_active ON session_items (session_id, is_active); -- 보유 아이템 조회용

COMMENT ON TABLE  session_items                  IS '세션 인벤토리';
COMMENT ON COLUMN session_items.session_id       IS '플레이 세션 아이디';
COMMENT ON COLUMN session_items.item_name        IS '아이템 이름. LLM 자유 생성';
COMMENT ON COLUMN session_items.item_description IS '아이템 설명. LLM 자유 생성';
COMMENT ON COLUMN session_items.grade            IS '아이템 등급. 서버가 결정';
COMMENT ON COLUMN session_items.wisdom_bonus     IS '지혜 보정치';
COMMENT ON COLUMN session_items.strength_bonus   IS '힘 보정치';
COMMENT ON COLUMN session_items.agility_bonus    IS '민첩 보정치';
COMMENT ON COLUMN session_items.charisma_bonus   IS '카리스마 보정치';
COMMENT ON COLUMN session_items.luck_bonus       IS '행운 보정치';
COMMENT ON COLUMN session_items.is_active        IS '보유 여부. false 면 버린 아이템';
COMMENT ON COLUMN session_items.gained_at_turn   IS '아이템 획득 턴 번호';
COMMENT ON COLUMN session_items.dropped_at_turn  IS '아이템 버린 턴 번호. NULL 이면 보유 중';


-- 세계관 평점 / 리뷰
-- user_id + world_id UNIQUE 로 세계관당 유저 1회만 평가 가능 (수정은 허용)
-- session_id 로 어느 세션 완료 후 남긴 평점인지 추적
-- review 는 선택 항목
CREATE TABLE IF NOT EXISTS world_ratings
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL,
    world_id   BIGINT    NOT NULL,
    session_id BIGINT    NOT NULL, -- 평점을 남긴 세션
    score      SMALLINT  NOT NULL CHECK (score BETWEEN 1 AND 5),
    review     VARCHAR(200),       -- 한줄 리뷰 (선택)
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_world_ratings_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_world_ratings_world FOREIGN KEY (world_id) REFERENCES worlds (id),
    CONSTRAINT fk_world_ratings_session FOREIGN KEY (session_id) REFERENCES story_sessions (id),
    UNIQUE (user_id, world_id)
);

CREATE INDEX IF NOT EXISTS idx_world_ratings_world_id ON world_ratings (world_id);
CREATE INDEX IF NOT EXISTS idx_world_ratings_user_id ON world_ratings (user_id);

COMMENT ON TABLE  world_ratings             IS '세계관 평점 및 리뷰';
COMMENT ON COLUMN world_ratings.user_id     IS '평점을 남긴 회원 아이디';
COMMENT ON COLUMN world_ratings.world_id    IS '평점을 남긴 세계관 아이디';
COMMENT ON COLUMN world_ratings.session_id  IS '평점을 남긴 세션 아이디';
COMMENT ON COLUMN world_ratings.score       IS '별점. 1점에서 5점';
COMMENT ON COLUMN world_ratings.review      IS '한줄 리뷰. 선택 항목';
COMMENT ON COLUMN world_ratings.created_at  IS '평점 등록 일시';
COMMENT ON COLUMN world_ratings.updated_at  IS '평점 수정 일시';


-- 신고
-- target_type 으로 신고 대상 종류를 구분 (리뷰 or 채팅 메시지)
-- target_id 는 target_type 에 따라 world_ratings.id or chat_messages.id 를 참조
-- status = PENDING : 처리 대기 / RESOLVED : 처리 완료 / DISMISSED : 신고 기각
-- handled_by, handled_at 은 처리 전 NULL
CREATE TABLE IF NOT EXISTS reports
(
    id          BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT       NOT NULL,
    target_type report_target_type  NOT NULL,                   -- 신고 타입 : REVIEW | CHAT_MESSAGE
    target_id   BIGINT       NOT NULL,                   -- 신고 대상 ID
    reason      TEXT NOT NULL,                   -- 신고 사유
    status      report_status  NOT NULL DEFAULT 'PENDING', -- 신고 상태 : PENDING | RESOLVED | DISMISSED
    handled_by  BIGINT,                                  -- 처리한 관리자 (처리 전 NULL)
    handled_at  TIMESTAMP,                               -- 처리 일시 (처리 전 NULL)
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users (id),
    CONSTRAINT fk_reports_handled_by FOREIGN KEY (handled_by) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_reports_status ON reports (status); -- 관리자 미처리 신고 조회용

COMMENT ON TABLE  reports             IS '신고';
COMMENT ON COLUMN reports.reporter_id IS '신고한 회원 아이디';
COMMENT ON COLUMN reports.target_type IS '신고 대상 종류. REVIEW, CHAT_MESSAGE';
COMMENT ON COLUMN reports.target_id   IS '신고 대상 아이디';
COMMENT ON COLUMN reports.reason      IS '신고 사유';
COMMENT ON COLUMN reports.status      IS '처리 상태. PENDING, RESOLVED, DISMISSED';
COMMENT ON COLUMN reports.handled_by  IS '처리한 관리자 아이디. 처리 전 NULL';
COMMENT ON COLUMN reports.handled_at  IS '처리 일시. 처리 전 NULL';
COMMENT ON COLUMN reports.created_at  IS '신고 등록 일시';

-- 기본 데이터 삽입

-- 아이템 등급 기준 초기값 (관리자가 수정 가능)
INSERT INTO item_grade_configs (grade, stat_sum_max, single_max, single_min, can_negative)
VALUES ('COMMON', 1, 1, 1, false), -- 단일 스탯 +1 고정
       ('RARE', 3, 2, 1, false),   -- 합산 최대 +3, 단일 +1~+2
       ('EPIC', 5, 3, 1, false),   -- 합산 최대 +5, 단일 +1~+3
       ('CURSED', 2, 4, -2, true)  -- 저주: 한 스탯 최대 +4, 다른 스탯 최소 -2
ON CONFLICT (grade) DO NOTHING;

-- 진행도 구간별 등급 확률 초기값 (관리자가 수정 가능)
INSERT INTO item_grade_probabilities (phase, grade, probability)
VALUES ('EARLY', 'COMMON', 60),
       ('EARLY', 'RARE', 30),
       ('EARLY', 'EPIC', 5),
       ('EARLY', 'CURSED', 5),
       ('MID', 'COMMON', 40),
       ('MID', 'RARE', 40),
       ('MID', 'EPIC', 15),
       ('MID', 'CURSED', 5),
       ('LATE', 'COMMON', 20),
       ('LATE', 'RARE', 40),
       ('LATE', 'EPIC', 30),
       ('LATE', 'CURSED', 10)
ON CONFLICT (phase, grade) DO NOTHING;
