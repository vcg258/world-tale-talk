ALTER TABLE world_ratings
    ADD count_play_number INT NOT NULL DEFAULT 0;

ALTER TABLE world_ratings
    DROP CONSTRAINT world_ratings_user_id_world_id_key;

DROP INDEX idx_world_ratings_session_id;
CREATE UNIQUE INDEX idx_world_ratings_session_id ON world_ratings (session_id);

COMMENT ON COLUMN world_ratings.count_play_number IS '해당 세계관 클리어 횟수';
