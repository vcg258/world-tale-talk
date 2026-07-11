-- 아이템 등급별 스탯 보정 설정 (이 데이터 없으면 아이템 등급 결정시 COMMON fallback)
INSERT INTO item_grade_configs (grade, stat_sum_max, single_max, single_min, can_negative)
VALUES ('COMMON', 2, 1, 0, false),
       ('RARE',   4, 2, 0, false),
       ('EPIC',   6, 3, 0, false),
       ('CURSED', 4, 3, -2, true);

-- 진행도 구간별 등급 확률 (동일 구간 합산 100)
INSERT INTO item_grade_probabilities (phase, grade, probability)
VALUES ('EARLY', 'COMMON', 55), ('EARLY', 'RARE', 30), ('EARLY', 'EPIC', 10), ('EARLY', 'CURSED', 5),
       ('MID',   'COMMON', 45), ('MID',   'RARE', 35), ('MID',   'EPIC', 15), ('MID',   'CURSED', 5),
       ('LATE',  'COMMON', 30), ('LATE',  'RARE', 35), ('LATE',  'EPIC', 25), ('LATE',  'CURSED', 10);

INSERT INTO users (nickname, email, password, role, status)
VALUES ('관리자개리', 'admin1@world.com', '$2a$10$X7..exampleBcryptHash60Chars.........................', 'ADMIN', 'ACTIVE'),
       ('관리자마크', 'admin2@world.com', '$2a$10$X7..exampleBcryptHash60Chars.........................', 'ADMIN', 'ACTIVE'),
       ('김판타지', 'user1@gmail.com', NULL, 'USER', 'ACTIVE'), -- 소셜 전용
       ('이무협', 'user2@naver.com', NULL, 'USER', 'ACTIVE'),   -- 소셜 전용
       ('정정지', 'user3@kakao.com', NULL, 'USER', 'SUSPENDED'); -- 현재 정지 상태 유저

INSERT INTO social_accounts (user_id, provider, provider_id)
VALUES (3, 'GOOGLE', 'google_oauth_unique_id_1111'),
       (3, 'KAKAO', 'kakao_oauth_unique_id_2222'), -- 중복 연동 케이스
       (4, 'NAVER', 'naver_oauth_unique_id_3333'),
       (5, 'KAKAO', 'kakao_oauth_unique_id_4444');

INSERT INTO user_suspensions (user_id, admin_id, reason, suspended_at, lifted_at)
VALUES (5, 1, '타인 비방 및 불적절한 채팅 메시지 다수 작성', NOW() - INTERVAL '3 DAY', NULL); -- 현재 정지 중

INSERT INTO notices (admin_id, title, content, is_active)
VALUES (1, '텍스트 RPG 서비스 오픈 안내', '안녕하세요. AI 기반 스토리 RPG에 오신 것을 환영합니다!', true),
       (2, '시스템 점검 안내 (완료)', '서버 안정화 작업이 완료되었습니다.', false),
       (1, '새 세계관 업데이트 안내', '신규 세계관이 추가되었습니다. 많은 이용 바랍니다.', true),
       (2, '이용약관 개정 안내', '개인정보 처리방침이 일부 개정되었습니다.', true),
       (1, '여름 이벤트 안내', '기간 한정 히든 엔딩 확률 2배 이벤트가 진행됩니다.', true),
       (2, '서버 점검 예정 안내', '다음 주 화요일 새벽 2시 정기 점검이 예정되어 있습니다.', false);

-- 세계관 (타겟 턴 10턴/15턴으로 예시 설정, 평균 평점은 리뷰 등록 후 갱신된다고 가정)
INSERT INTO worlds (name, genre, description, setting_text, target_turn, is_active, rating_avg, play_count)
VALUES ('엘프헤임의 몰락', 'FANTASY', '마법이 고갈되어 가는 세계에서 생존하세요.', '당신은 마법 고갈 사태를 맞이한 하이엘프 왕국의 주민입니다...', 10, true, 4.50, 2),
       ('강호행 각성록', 'MARTIAL_ARTS', '배신당한 문파의 후계자가 되어 복수하세요.', '중원 무림의 화산파는 마교의 기습으로 멸문 위기에 처했습니다...', 15, true, 5.00, 1);

-- 페르소나 (엘프헤임 캐릭터 2명)
INSERT INTO personas (world_id, name, description, background, is_active)
VALUES (1, '아일린', '왕국의 마지막 마법학자', '상류층 출신으로 마법을 되살릴 단서를 찾고 있습니다.', true),
       (1, '카엘', '그림자 추적자 용병', '돈만 주면 무엇이든 하는 냉혈한 냉소주의자입니다.', true);

-- 페르소나 기본 스탯 (1:1 관계)
INSERT INTO persona_base_stats (persona_id, wisdom, strength, agility, charisma, luck)
VALUES (1, 9, 3, 5, 6, 4), -- 아일린 스탯
       (2, 4, 7, 8, 3, 5); -- 카엘 스탯

-- 페르소나 (강호행 캐릭터 2명)
INSERT INTO personas (world_id, name, description, background, is_active)
VALUES (2, '백운비', '화산파의 생존한 대제자', '사부님의 유언에 따라 문파의 비급을 지키려 합니다.', true),
       (2, '설화', '만천홍루의 정보원', '기녀로 위장하고 있으나 실제로는 무림의 정보를 쥐고 흔듭니다.', true);

INSERT INTO persona_base_stats (persona_id, wisdom, strength, agility, charisma, luck)
VALUES (3, 6, 8, 6, 5, 3), -- 백운비 스탯
       (4, 8, 4, 7, 7, 5); -- 설화 스탯

-- 세션 1: 김판타지가 아일린으로 플레이하여 엔딩 컴플리트한 세션
-- 세션 2: 이무협이 백운비로 플레이하여 현재 진행 중인 세션
-- 세션 3: 김판타지가 카엘로 플레이하다가 도중에 포기(ABANDONED)한 세션
INSERT INTO story_sessions (user_id, world_id, persona_id, status, current_turn, ending_type, hidden_by_user)
VALUES (3, 1, 1, 'COMPLETED', 10, 'TRUE', false),
       (4, 2, 3, 'IN_PROGRESS', 2, NULL, true),
       (3, 1, 2, 'ABANDONED', 1, NULL, true);

-- 세션 1 (완료됨)의 최종 확정 스탯 스냅샷 (1:1 관계)
INSERT INTO session_stat_snapshots (session_id, wisdom, strength, agility, charisma, luck)
VALUES (1, 13, 3, 6, 7, 5);

-- 세션 1의 턴별 스탯 변화 로그 (기본 스탯에 더해짐)
INSERT INTO session_stat_logs (session_id, turn, wisdom_delta, strength_delta, agility_delta, charisma_delta, luck_delta)
VALUES (1, 1, 2, 0, 0, 1, 0),  -- 1턴: 지혜+2, 카리스마+1
       (1, 4, 1, 0, 1, 0, 0),  -- 4턴: 지혜+1, 민첩+1
       (1, 8, 1, 0, 0, 0, 1);  -- 8턴: 지혜+1, 행운+1

-- 세션 2 (진행 중)의 스탯 변화 로그
INSERT INTO session_stat_logs (session_id, turn, wisdom_delta, strength_delta, agility_delta, charisma_delta, luck_delta)
VALUES (2, 1, 0, 1, 0, 0, 0);  -- 1턴: 힘+1

-- 세션 4: 이무협이 설화로 플레이하여 GOOD 엔딩 도달한 세션
-- 세션 5: 김판타지가 아일린으로 플레이하여 BAD 엔딩 도달한 세션
-- 세션 6: 이무협이 백운비로 플레이하여 DARK 엔딩 도달한 세션
INSERT INTO story_sessions (user_id, world_id, persona_id, status, current_turn, ending_type)
VALUES (4, 2, 4, 'COMPLETED', 15, 'GOOD'),
       (3, 1, 1, 'COMPLETED', 10, 'BAD'),
       (4, 2, 3, 'COMPLETED', 10, 'DARK');

-- 세션 4,5,6 최종 스탯 스냅샷
INSERT INTO session_stat_snapshots (session_id, wisdom, strength, agility, charisma, luck)
VALUES (4, 10, 5, 9, 11, 7),  -- GOOD 엔딩 : 전체 delta 합산 높음
       (5, 7,  3, 4, 5,  3),  -- BAD 엔딩 : 전체 delta 합산 낮음
       (6, 3,  9, 7, 2,  3);  -- DARK 엔딩 : 지혜 + 카리스마 기본값 이하

-- 세션 4,5,6 스탯 로그
INSERT INTO session_stat_logs (session_id, turn, wisdom_delta, strength_delta, agility_delta, charisma_delta, luck_delta)
VALUES (4, 3, 2, 0, 1, 2, 1), (4, 7, 1, 1, 1, 2, 1),   -- GOOD: 전체 누적 +12
       (5, 2, -1, -1, -1, -1, -1), (5, 6, -1, 0, -1, -1, 0), -- BAD: 전체 누적 -7
       (6, 2, -3, 2, 1, -3, 0), (6, 5, -3, 0, 0, -4, 0);  -- DARK: 지혜+카리스마 기본값 이하

-- 세션 1 (커서 페이징 테스트용 - 24개, 1분 간격으로 타임스탬프 부여)
-- 최초 요청: 최신 20개 반환 (turn5 USER ~ turn12 USER)
-- 커서 요청: turn5 USER의 createdAt 기준 -> 나머지 4개 (turn1~turn4) 반환
INSERT INTO chat_messages (session_id, turn, role, content, is_epilogue, created_at, updated_at)
VALUES (1, 1,  'ASSISTANT', '마법 학당의 무너진 천장 사이로 붉은 달빛이 새어 들어옵니다. 아일린, 당신은 무엇을 합니까?', false, NOW() - INTERVAL '23 minutes', NOW() - INTERVAL '23 minutes'),
       (1, 1,  'USER',      '남아있는 마법 서적을 챙겨서 가방에 넣고 주위를 경계해.',                                   false, NOW() - INTERVAL '22 minutes', NOW() - INTERVAL '22 minutes'),
       (1, 2,  'ASSISTANT', '마법 서적 중 하나에서 희미한 빛이 흘러나옵니다. 당신은 어떻게 하겠습니까?',               false, NOW() - INTERVAL '21 minutes', NOW() - INTERVAL '21 minutes'),
       (1, 2,  'USER',      '빛이 나오는 책을 조심스럽게 펼쳐본다.',                                                   false, NOW() - INTERVAL '20 minutes', NOW() - INTERVAL '20 minutes'),
       (1, 3,  'ASSISTANT', '책 속에서 고대 마법진의 설계도가 나타났습니다. 이것이 세계를 구할 열쇠일 수 있습니다.',    false, NOW() - INTERVAL '19 minutes', NOW() - INTERVAL '19 minutes'),
       (1, 3,  'USER',      '설계도를 암기하고 다음 단서를 찾아 학당 깊숙이 들어간다.',                                false, NOW() - INTERVAL '18 minutes', NOW() - INTERVAL '18 minutes'),
       (1, 4,  'ASSISTANT', '학당 지하실 입구를 발견했습니다. 안에서 이상한 소리가 들려옵니다.',                        false, NOW() - INTERVAL '17 minutes', NOW() - INTERVAL '17 minutes'),
       (1, 4,  'USER',      '마법으로 소리의 근원을 탐지한다.',                                                        false, NOW() - INTERVAL '16 minutes', NOW() - INTERVAL '16 minutes'),
       (1, 5,  'ASSISTANT', '지하실 안에 마법이 고갈되어 방황하는 정령이 있습니다. 당신은 어떻게 하겠습니까?',          false, NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '15 minutes'),
       (1, 5,  'USER',      '정령에게 말을 걸어 도움을 줄 수 있는지 물어본다.',                                        false, NOW() - INTERVAL '14 minutes', NOW() - INTERVAL '14 minutes'),
       (1, 6,  'ASSISTANT', '정령이 당신의 말에 반응하며 마법진의 핵심 위치를 알려주었습니다.',                         false, NOW() - INTERVAL '13 minutes', NOW() - INTERVAL '13 minutes'),
       (1, 6,  'USER',      '정령에게 감사 인사를 하고 알려준 장소로 향한다.',                                         false, NOW() - INTERVAL '12 minutes', NOW() - INTERVAL '12 minutes'),
       (1, 7,  'ASSISTANT', '마법진의 핵심 장치가 있는 제단에 도착했습니다. 장치가 손상되어 있습니다.',                 false, NOW() - INTERVAL '11 minutes', NOW() - INTERVAL '11 minutes'),
       (1, 7,  'USER',      '수습한 마법 서적의 지식으로 장치를 수리해본다.',                                          false, NOW() - INTERVAL '10 minutes', NOW() - INTERVAL '10 minutes'),
       (1, 8,  'ASSISTANT', '수리가 거의 완료되었습니다. 마지막 마나 결정이 필요합니다.',                               false, NOW() - INTERVAL '9 minutes',  NOW() - INTERVAL '9 minutes'),
       (1, 8,  'USER',      '내 마나를 모두 쏟아부어 마나 결정을 생성한다.',                                           false, NOW() - INTERVAL '8 minutes',  NOW() - INTERVAL '8 minutes'),
       (1, 9,  'ASSISTANT', '마나 결정이 완성되었습니다. 마법진에 끼워넣는 순간 세계가 진동합니다.',                    false, NOW() - INTERVAL '7 minutes',  NOW() - INTERVAL '7 minutes'),
       (1, 9,  'USER',      '마나 결정을 마법진에 삽입한다.',                                                          false, NOW() - INTERVAL '6 minutes',  NOW() - INTERVAL '6 minutes'),
       (1, 10, 'ASSISTANT', '당신의 선택으로 고대 마법진이 활성화되었습니다. 세계는 구원받았습니다. (엔딩 도달)',       false, NOW() - INTERVAL '5 minutes',  NOW() - INTERVAL '5 minutes'),
       (1, 10, 'USER',      '해냈어. 이제 마법이 돌아올 거야.',                                                        false, NOW() - INTERVAL '4 minutes',  NOW() - INTERVAL '4 minutes'),
       (1, 11, 'ASSISTANT', '그 후 아일린은 대마법사로 칭송받으며 평화로운 나날을 보냅니다.',                           true,  NOW() - INTERVAL '3 minutes',  NOW() - INTERVAL '3 minutes'),
       (1, 11, 'USER',      '다행이야. 책을 지켜낸 보람이 있었네.',                                                    true,  NOW() - INTERVAL '2 minutes',  NOW() - INTERVAL '2 minutes'),
       (1, 12, 'ASSISTANT', '왕국의 마법사들이 하나둘 마법을 되찾기 시작했습니다.',                                     true,  NOW() - INTERVAL '1 minutes',  NOW() - INTERVAL '1 minutes'),
       (1, 12, 'USER',      '모두가 마법을 쓸 수 있는 세상이 됐으면 좋겠어.',                                          true,  NOW(),                          NOW());

-- 세션 2 (현재 2턴 진행 중인 로그)
INSERT INTO chat_messages (session_id, turn, role, content, is_epilogue)
VALUES (2, 1, 'ASSISTANT', '화산파의 연무장에는 핏자국만이 가득합니다. 백운비, 검을 쥔 손에 힘이 들어갑니다.', false),
       (2, 1, 'USER', '사부님의 유품인 검을 수습하고 산문 아래로 내려간다.', false),
       (2, 2, 'ASSISTANT', '산길을 내려가던 중, 검은 복면을 쓴 무인 두 명이 앞을 가로막습니다.', false);

-- 세션 1에서 획득하여 끝까지 보유한 레어 아이템 (실제 스탯 계산에 반영됨)
INSERT INTO session_items (session_id, item_name, item_description, grade, wisdom_bonus, strength_bonus, agility_bonus, charisma_bonus, luck_bonus, is_active, gained_at_turn, dropped_at_turn)
VALUES (1, '고대 고서의 잔해', '마법 지식이 담긴 찢어진 책장', 'RARE', 2, 0, 0, 0, 0, true, 2, NULL);

-- 세션 2에서 획득하여 보유 중인 저주받은 아이템
INSERT INTO session_items (session_id, item_name, item_description, grade, wisdom_bonus, strength_bonus, agility_bonus, charisma_bonus, luck_bonus, is_active, gained_at_turn, dropped_at_turn)
VALUES (2, '핏빛 묻은 단검', '기분 나쁜 기운이 흘러나오는 단검', 'CURSED', 0, 2, 0, 0, -1, true, 1, NULL);

-- 김판타지가 세션 1을 끝내고 엘프헤임 세계관에 남긴 평점
INSERT INTO world_ratings (user_id, world_id, session_id, score, review)
VALUES (3, 1, 1, 5, '스토리가 웅장하고 몰입감이 최고입니다. 에필로그도 만족스러워요.');

-- 이무협이 세션 2 진행 중에 강호행 세계관에 남긴 평점
INSERT INTO world_ratings (user_id, world_id, session_id, score, review)
VALUES (4, 2, 2, 5, '무협 감성 지 대로네요. 백운비 최고.');

-- 유저의 리뷰에 대해 다른 유저가 신고를 하였고 관리자가 처리 중이거나 완료한 상태 구성
INSERT INTO reports (reporter_id, target_type, target_id, reason, status, handled_by, handled_at)
VALUES (3, 'REVIEW',       2, '리뷰에 광고성 외계어가 포함되어 있습니다.', 'PENDING',   NULL, NULL),                   -- 대기 중
       (4, 'CHAT_MESSAGE', 5, '채팅 중 심한 욕설을 사용했습니다.',         'RESOLVED',  1,    NOW() - INTERVAL '3 DAY'), -- 처리 완료
       (4, 'REVIEW',       1, '허위 과장 광고성 리뷰로 의심됩니다.',        'PENDING',   NULL, NULL),                   -- 대기 중
       (3, 'CHAT_MESSAGE', 3, '맥락 없는 스팸성 메시지를 반복했습니다.',    'DISMISSED', 2,    NOW() - INTERVAL '1 DAY'), -- 반려
       (5, 'REVIEW',       2, '동일 내용 중복 리뷰입니다.',                 'DISMISSED', 1,    NOW() - INTERVAL '5 DAY'); -- 반려