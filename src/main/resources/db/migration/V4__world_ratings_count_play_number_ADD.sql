ALTER TABLE world_ratings
    ADD count_play_number INT NOT NULL DEFAULT 0;
COMMENT ON COLUMN world_ratings.count_play_number IS '해당 세계관 클리어 횟수';
