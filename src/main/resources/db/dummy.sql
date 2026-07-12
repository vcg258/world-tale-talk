-- 개발/화면 확인용 더미 데이터입니다.
-- world_tale_talk 데이터베이스에 접속한 상태에서 수동 실행하세요.
-- 주의: 아래 TRUNCATE는 앱 도메인 데이터를 초기화합니다. Flyway/Batch/Vector 테이블은 건드리지 않습니다.

BEGIN;

TRUNCATE TABLE
    reports,
    world_ratings,
    session_items,
    chat_messages,
    session_stat_logs,
    session_stat_snapshots,
    story_sessions,
    persona_base_stats,
    personas,
    notices,
    user_suspensions,
    social_accounts,
    dormant_notifications,
    item_grade_probabilities,
    item_grade_configs,
    worlds,
    users
RESTART IDENTITY CASCADE;

-- 일반 로그인 확인:
--   admin / admin1234
--   user1 / user1234
--   user2 / user1234
-- DelegatingPasswordEncoder가 지원하는 {noop} 개발용 비밀번호입니다.
INSERT INTO users (login_id, nickname, email, password, role, status, last_login_at)
VALUES
    ('admin', '관리자개리', 'admin1@world.com', '{noop}admin1234', 'ADMIN', 'ACTIVE', NOW()),
    ('admin2', '관리자마크', 'admin2@world.com', '{noop}admin1234', 'ADMIN', 'ACTIVE', NOW() - INTERVAL '2 DAY'),
    ('user1', '김판타지', 'user1@gmail.com', '{noop}user1234', 'USER', 'ACTIVE', NOW()),
    ('user2', '이무협', 'user2@naver.com', '{noop}user1234', 'USER', 'ACTIVE', NOW() - INTERVAL '1 DAY'),
    ('user3', '정정지', 'user3@kakao.com', '{noop}user1234', 'USER', 'SUSPENDED', NOW() - INTERVAL '10 DAY'),
    ('dormant1', '휴면나그네', 'dormant1@world.com', '{noop}user1234', 'USER', 'DORMANT', NOW() - INTERVAL '420 DAY');

INSERT INTO social_accounts (user_id, provider, provider_id)
VALUES
    (3, 'GOOGLE', 'google_oauth_unique_id_1111'),
    (3, 'KAKAO', 'kakao_oauth_unique_id_2222'),
    (4, 'NAVER', 'naver_oauth_unique_id_3333'),
    (5, 'KAKAO', 'kakao_oauth_unique_id_4444');

INSERT INTO user_suspensions (user_id, admin_id, reason, suspended_at, lifted_at)
VALUES
    (5, 1, '타인 비방 및 불적절한 채팅 메시지 다수 작성', NOW() - INTERVAL '3 DAY', NULL),
    (4, 2, '과거 채팅 신고 누적으로 1일 정지', NOW() - INTERVAL '20 DAY', NOW() - INTERVAL '19 DAY');

INSERT INTO dormant_notifications (id, user_id, notified_at)
VALUES
    (1, 6, NOW() - INTERVAL '30 DAY');

INSERT INTO item_grade_configs (grade, stat_sum_max, single_max, single_min, can_negative)
VALUES
    ('COMMON', 2, 1, 0, false),
    ('RARE', 4, 2, 0, false),
    ('EPIC', 6, 3, 0, false),
    ('CURSED', 4, 3, -2, true);

INSERT INTO item_grade_probabilities (phase, grade, probability)
VALUES
    ('EARLY', 'COMMON', 55), ('EARLY', 'RARE', 30), ('EARLY', 'EPIC', 10), ('EARLY', 'CURSED', 5),
    ('MID', 'COMMON', 45), ('MID', 'RARE', 35), ('MID', 'EPIC', 15), ('MID', 'CURSED', 5),
    ('LATE', 'COMMON', 30), ('LATE', 'RARE', 35), ('LATE', 'EPIC', 25), ('LATE', 'CURSED', 10);

INSERT INTO notices (admin_id, title, content, is_active)
VALUES
    (1, '텍스트 RPG 서비스 오픈 안내', '안녕하세요. AI 기반 스토리 RPG에 오신 것을 환영합니다!', true),
    (2, '시스템 점검 안내 (완료)', '서버 안정화 작업이 완료되었습니다.', false),
    (1, '새 세계관 업데이트 안내', '신규 세계관이 추가되었습니다. 많은 이용 바랍니다.', true),
    (2, '이용약관 개정 안내', '개인정보 처리방침이 일부 개정되었습니다.', true),
    (1, '여름 이벤트 안내', '기간 한정 히든 엔딩 확률 2배 이벤트가 진행됩니다.', true),
    (2, '서버 점검 예정 안내', '다음 주 화요일 새벽 2시 정기 점검이 예정되어 있습니다.', false);

INSERT INTO worlds (name, genre, description, setting_text, target_turn, is_active, rating_avg, play_count)
VALUES
    ('엘프헤임의 몰락', 'FANTASY', '마법이 고갈되어 가는 세계에서 생존하세요.', '마법 고갈 사태를 맞이한 하이엘프 왕국. 플레이어는 왕국의 붕괴를 막기 위해 고대 마법진의 비밀을 추적한다.', 10, true, 4.67, 4),
    ('강호행 각성록', 'MARTIAL_ARTS', '배신당한 문파의 후계자가 되어 복수하세요.', '중원 무림의 화산파는 마교의 기습으로 멸문 위기에 처했다. 생존자는 비급과 진실을 찾아 강호로 내려간다.', 15, true, 4.50, 3),
    ('네온 오르비스', 'SF', '거대 우주도시의 음모를 파헤치는 사이버 모험입니다.', '궤도 도시 오르비스에서 기억 거래와 인공지능 재판이 일상화되었다. 플레이어는 사라진 기억 조각을 추적한다.', 12, true, 4.00, 1),
    ('안개 저택의 초대장', 'MYSTERY', '고립된 저택에서 벌어지는 추리극입니다.', '폭풍우가 몰아치는 밤, 여섯 명의 손님이 안개 저택에 초대된다. 첫 번째 종소리와 함께 주인이 사라진다.', 8, false, 0.00, 0);

INSERT INTO personas (world_id, name, description, background, is_active)
VALUES
    (1, '아일린', '왕국의 마지막 마법학자', '상류층 출신으로 마법을 되살릴 단서를 찾고 있다. 지식과 설득에 강하지만 육체 전투에는 약하다.', true),
    (1, '카엘', '그림자 추적자 용병', '돈만 주면 무엇이든 하는 냉소적인 용병. 빠른 판단과 은밀한 행동에 능하다.', true),
    (2, '백운비', '화산파의 생존한 대제자', '사부님의 유언에 따라 문파의 비급을 지키려 한다. 정면 돌파와 의리를 중시한다.', true),
    (2, '설화', '만천홍루의 정보원', '기녀로 위장하고 있으나 실제로는 무림의 정보를 쥐고 흔든다. 매력과 지략이 뛰어나다.', true),
    (3, '리안 제로', '기억을 잃은 해커', '오르비스 하층에서 깨어난 해커. 자신이 훔친 기억의 정체를 알지 못한다.', true),
    (3, '노아-17', '감정을 배운 안드로이드', '폐기 예정이었으나 자아를 얻은 안드로이드. 논리적이지만 인간의 감정을 탐구한다.', true),
    (4, '윤서', '초대장을 받은 기자', '실종 사건을 취재하던 중 안개 저택의 초대장을 받았다.', true);

INSERT INTO persona_base_stats (persona_id, wisdom, strength, agility, charisma, luck)
VALUES
    (1, 9, 3, 5, 6, 4),
    (2, 4, 7, 8, 3, 5),
    (3, 6, 8, 6, 5, 3),
    (4, 8, 4, 7, 7, 5),
    (5, 8, 3, 8, 4, 6),
    (6, 7, 6, 5, 3, 5),
    (7, 9, 2, 5, 8, 4);

INSERT INTO story_sessions (user_id, world_id, persona_id, status, current_turn, ending_type, running_summary, hidden_by_user, created_at, updated_at)
VALUES
    (3, 1, 1, 'COMPLETED', 10, 'TRUE', '아일린은 고대 마법진을 복원해 엘프헤임의 마력을 되살렸다.', false, NOW() - INTERVAL '7 DAY', NOW() - INTERVAL '6 DAY'),
    (4, 2, 3, 'IN_PROGRESS', 2, NULL, '백운비는 화산파 참사의 단서를 찾아 산문 아래로 내려갔다.', false, NOW() - INTERVAL '2 HOUR', NOW() - INTERVAL '20 MINUTE'),
    (3, 1, 2, 'ABANDONED', 1, NULL, '카엘은 폐허가 된 왕성의 비밀 통로를 발견했으나 추적을 중단했다.', true, NOW() - INTERVAL '5 DAY', NOW() - INTERVAL '5 DAY'),
    (4, 2, 4, 'COMPLETED', 15, 'GOOD', '설화는 무림의 음모를 폭로하고 백성들을 구했다.', false, NOW() - INTERVAL '4 DAY', NOW() - INTERVAL '3 DAY'),
    (3, 1, 1, 'COMPLETED', 10, 'BAD', '아일린은 마법진 복원에 실패했고 왕국은 긴 겨울에 잠겼다.', false, NOW() - INTERVAL '3 DAY', NOW() - INTERVAL '2 DAY'),
    (4, 2, 3, 'COMPLETED', 10, 'DARK', '백운비는 복수에 성공했지만 마교보다 차가운 검귀가 되었다.', false, NOW() - INTERVAL '2 DAY', NOW() - INTERVAL '1 DAY'),
    (3, 3, 5, 'ENDING', 12, 'SECRET', '리안은 자신의 기억이 도시 중앙 AI의 열쇠임을 깨달았다.', false, NOW() - INTERVAL '1 DAY', NOW() - INTERVAL '1 HOUR'),
    (3, 3, 6, 'EPILOGUE', 12, 'GOOD', '노아-17은 오르비스 시민권을 인정받고 첫 선택을 앞두고 있다.', false, NOW() - INTERVAL '12 HOUR', NOW() - INTERVAL '30 MINUTE');

INSERT INTO session_stat_snapshots (session_id, wisdom, strength, agility, charisma, luck, story_summary)
VALUES
    (1, 13, 3, 6, 7, 5, '마법진 복원 성공. TRUE 엔딩.'),
    (2, 6, 9, 6, 5, 3, '복면 무인과 대치 중.'),
    (3, 4, 7, 8, 3, 5, '왕성 잠입 중단.'),
    (4, 10, 5, 9, 11, 7, '강호의 음모를 막은 GOOD 엔딩.'),
    (5, 7, 3, 4, 5, 3, '마법진 복원 실패 BAD 엔딩.'),
    (6, 3, 9, 7, 2, 3, '복수심에 잠식된 DARK 엔딩.'),
    (7, 11, 4, 10, 5, 8, 'SECRET 엔딩 직전.'),
    (8, 9, 7, 6, 5, 6, '에필로그 진행 중.');

INSERT INTO session_stat_logs (session_id, turn, wisdom_delta, strength_delta, agility_delta, charisma_delta, luck_delta)
VALUES
    (1, 1, 2, 0, 0, 1, 0), (1, 4, 1, 0, 1, 0, 0), (1, 8, 1, 0, 0, 0, 1),
    (2, 1, 0, 1, 0, 0, 0), (2, 2, 0, 0, 0, 0, 1),
    (3, 1, 0, 0, 0, 0, -1),
    (4, 3, 2, 0, 1, 2, 1), (4, 7, 1, 1, 1, 2, 1),
    (5, 2, -1, -1, -1, -1, -1), (5, 6, -1, 0, -1, -1, 0),
    (6, 2, -3, 2, 1, -3, 0), (6, 5, -3, 0, 0, -4, 0),
    (7, 4, 2, 0, 1, 0, 1), (7, 10, 1, 1, 1, 1, 1),
    (8, 6, 1, 1, 0, 1, 0);

INSERT INTO chat_messages (session_id, turn, role, content, is_epilogue, created_at, updated_at)
VALUES
    (1, 1, 'ASSISTANT', '마법 학당의 무너진 천장 사이로 붉은 달빛이 새어 들어옵니다. 아일린, 당신은 무엇을 합니까?', false, NOW() - INTERVAL '23 minutes', NOW() - INTERVAL '23 minutes'),
    (1, 1, 'USER', '남아있는 마법 서적을 챙겨서 가방에 넣고 주위를 경계해.', false, NOW() - INTERVAL '22 minutes', NOW() - INTERVAL '22 minutes'),
    (1, 2, 'ASSISTANT', '마법 서적 중 하나에서 희미한 빛이 흘러나옵니다. 당신은 어떻게 하겠습니까?', false, NOW() - INTERVAL '21 minutes', NOW() - INTERVAL '21 minutes'),
    (1, 2, 'USER', '빛이 나오는 책을 조심스럽게 펼쳐본다.', false, NOW() - INTERVAL '20 minutes', NOW() - INTERVAL '20 minutes'),
    (1, 3, 'ASSISTANT', '책 속에서 고대 마법진의 설계도가 나타났습니다. 이것이 세계를 구할 열쇠일 수 있습니다.', false, NOW() - INTERVAL '19 minutes', NOW() - INTERVAL '19 minutes'),
    (1, 3, 'USER', '설계도를 암기하고 다음 단서를 찾아 학당 깊숙이 들어간다.', false, NOW() - INTERVAL '18 minutes', NOW() - INTERVAL '18 minutes'),
    (1, 4, 'ASSISTANT', '학당 지하실 입구를 발견했습니다. 안에서 이상한 소리가 들려옵니다.', false, NOW() - INTERVAL '17 minutes', NOW() - INTERVAL '17 minutes'),
    (1, 4, 'USER', '마법으로 소리의 근원을 탐지한다.', false, NOW() - INTERVAL '16 minutes', NOW() - INTERVAL '16 minutes'),
    (1, 5, 'ASSISTANT', '지하실 안에 마법이 고갈되어 방황하는 정령이 있습니다. 당신은 어떻게 하겠습니까?', false, NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes'),
    (1, 5, 'USER', '정령에게 말을 걸어 도움을 줄 수 있는지 물어본다.', false, NOW() - INTERVAL '14 minutes', NOW() - INTERVAL '14 minutes'),
    (1, 6, 'ASSISTANT', '정령이 당신의 말에 반응하며 마법진의 핵심 위치를 알려주었습니다.', false, NOW() - INTERVAL '13 minutes', NOW() - INTERVAL '13 minutes'),
    (1, 6, 'USER', '정령에게 감사 인사를 하고 알려준 장소로 향한다.', false, NOW() - INTERVAL '12 minutes', NOW() - INTERVAL '12 minutes'),
    (1, 7, 'ASSISTANT', '마법진의 핵심 장치가 있는 제단에 도착했습니다. 장치가 손상되어 있습니다.', false, NOW() - INTERVAL '11 minutes', NOW() - INTERVAL '11 minutes'),
    (1, 7, 'USER', '수습한 마법 서적의 지식으로 장치를 수리해본다.', false, NOW() - INTERVAL '10 minutes', NOW() - INTERVAL '10 minutes'),
    (1, 8, 'ASSISTANT', '수리가 거의 완료되었습니다. 마지막 마나 결정이 필요합니다.', false, NOW() - INTERVAL '9 minutes', NOW() - INTERVAL '9 minutes'),
    (1, 8, 'USER', '내 마나를 모두 쏟아부어 마나 결정을 생성한다.', false, NOW() - INTERVAL '8 minutes', NOW() - INTERVAL '8 minutes'),
    (1, 9, 'ASSISTANT', '마나 결정이 완성되었습니다. 마법진에 끼워넣는 순간 세계가 진동합니다.', false, NOW() - INTERVAL '7 minutes', NOW() - INTERVAL '7 minutes'),
    (1, 9, 'USER', '마나 결정을 마법진에 삽입한다.', false, NOW() - INTERVAL '6 minutes', NOW() - INTERVAL '6 minutes'),
    (1, 10, 'ASSISTANT', '당신의 선택으로 고대 마법진이 활성화되었습니다. 세계는 구원받았습니다. TRUE 엔딩에 도달했습니다.', false, NOW() - INTERVAL '5 minutes', NOW() - INTERVAL '5 minutes'),
    (1, 10, 'USER', '해냈어. 이제 마법이 돌아올 거야.', false, NOW() - INTERVAL '4 minutes', NOW() - INTERVAL '4 minutes'),
    (1, 11, 'ASSISTANT', '그 후 아일린은 대마법사로 칭송받으며 평화로운 나날을 보냅니다.', true, NOW() - INTERVAL '3 minutes', NOW() - INTERVAL '3 minutes'),
    (1, 11, 'USER', '다행이야. 책을 지켜낸 보람이 있었네.', true, NOW() - INTERVAL '2 minutes', NOW() - INTERVAL '2 minutes'),
    (1, 12, 'ASSISTANT', '왕국의 마법사들이 하나둘 마법을 되찾기 시작했습니다.', true, NOW() - INTERVAL '1 minutes', NOW() - INTERVAL '1 minutes'),
    (1, 12, 'USER', '모두가 마법을 쓸 수 있는 세상이 됐으면 좋겠어.', true, NOW(), NOW()),
    (2, 1, 'ASSISTANT', '화산파의 연무장에는 핏자국만이 가득합니다. 백운비, 검을 쥔 손에 힘이 들어갑니다.', false, NOW() - INTERVAL '16 minutes', NOW() - INTERVAL '16 minutes'),
    (2, 1, 'USER', '사부님의 유품인 검을 수습하고 산문 아래로 내려간다.', false, NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes'),
    (2, 2, 'ASSISTANT', '산길을 내려가던 중, 검은 복면을 쓴 무인 두 명이 앞을 가로막습니다.', false, NOW() - INTERVAL '14 minutes', NOW() - INTERVAL '14 minutes'),
    (7, 12, 'ASSISTANT', '오르비스 중앙 AI가 마지막 선택을 요구합니다. 기억을 공개하면 도시는 자유로워집니다.', false, NOW() - INTERVAL '8 minutes', NOW() - INTERVAL '8 minutes'),
    (8, 13, 'ASSISTANT', '노아-17은 처음으로 자신의 이름을 직접 선택합니다.', true, NOW() - INTERVAL '6 minutes', NOW() - INTERVAL '6 minutes'),
    (8, 13, 'USER', '나는 노아라는 이름으로 남고 싶어.', true, NOW() - INTERVAL '5 minutes', NOW() - INTERVAL '5 minutes');

INSERT INTO session_items (session_id, item_name, item_description, grade, wisdom_bonus, strength_bonus, agility_bonus, charisma_bonus, luck_bonus, is_active, gained_at_turn, dropped_at_turn)
VALUES
    (1, '고대 고서의 잔해', '마법 지식이 담긴 찢어진 책장', 'RARE', 2, 0, 0, 0, 0, true, 2, NULL),
    (1, '정령의 은빛 눈물', '잃어버린 마나를 잠시 되살리는 작은 결정', 'EPIC', 1, 0, 0, 1, 1, true, 6, NULL),
    (2, '핏빛 묻은 단검', '기분 나쁜 기운이 흘러나오는 단검', 'CURSED', 0, 2, 0, 0, -1, true, 1, NULL),
    (3, '부러진 흑단 단검', '임무 포기 후 버려진 단검', 'COMMON', 0, 1, 0, 0, 0, false, 1, 1),
    (7, '불법 메모리 칩', '금지된 기억 조각이 담긴 칩', 'EPIC', 2, 0, 2, 0, 1, true, 5, NULL);

INSERT INTO world_ratings (user_id, world_id, session_id, score, review, count_play_number)
VALUES
    (3, 1, 1, 5, '스토리가 웅장하고 몰입감이 최고입니다. 에필로그도 만족스러워요.', 1),
    (4, 2, 4, 5, '설화 루트가 정말 매력적입니다. 무협 정치극 느낌이 좋았어요.', 1),
    (3, 1, 5, 4, 'BAD 엔딩도 여운이 깊어서 다시 TRUE 엔딩을 보고 싶어졌습니다.', 2),
    (4, 2, 6, 4, '백운비 DARK 엔딩은 묵직하고 강렬했습니다.', 2),
    (3, 3, 8, 4, 'SF 세계관 분위기가 좋아요. 다음에는 SECRET 엔딩을 보고 싶습니다.', 1);

INSERT INTO reports (reporter_id, target_type, target_id, reason, status, handled_by, handled_at)
VALUES
    (3, 'REVIEW', 2, '리뷰에 광고성 외계어가 포함되어 있습니다.', 'PENDING', NULL, NULL),
    (4, 'CHAT_MESSAGE', 5, '채팅 중 심한 욕설을 사용했습니다.', 'RESOLVED', 1, NOW() - INTERVAL '3 DAY'),
    (4, 'REVIEW', 1, '허위 과장 광고성 리뷰로 의심됩니다.', 'PENDING', NULL, NULL),
    (3, 'CHAT_MESSAGE', 3, '맥락 없는 스팸성 메시지를 반복했습니다.', 'DISMISSED', 2, NOW() - INTERVAL '1 DAY'),
    (5, 'REVIEW', 3, '동일 내용 중복 리뷰입니다.', 'DISMISSED', 1, NOW() - INTERVAL '5 DAY');

COMMIT;
