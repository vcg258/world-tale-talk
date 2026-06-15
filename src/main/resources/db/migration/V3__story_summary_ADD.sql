ALTER TABLE session_stat_snapshots
    ADD story_summary TEXT;
COMMENT ON COLUMN session_stat_snapshots.story_summary IS '엔딩 시점 메인 스토리 요약본';