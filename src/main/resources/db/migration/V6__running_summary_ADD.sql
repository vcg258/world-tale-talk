ALTER TABLE story_sessions
    ADD running_summary TEXT;

COMMENT ON COLUMN story_sessions.running_summary IS '지정 턴마다 갱신되는 스토리 요약본'