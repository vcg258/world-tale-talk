package com.worldtaletalk.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorldPersonaPurgeJdbcService {
    private final NamedParameterJdbcTemplate jdbc;

    /**
     * 세계관 완전 삭제 (하드 삭제)
     *
     * @param worldId 세계관 PK
     */
    @Transactional
    public void purgeWorld(Long worldId) {
        Map<String, Object> world = jdbc.queryForMap("""
                SELECT * FROM worlds WHERE id = :worldId
                """, new MapSqlParameterSource("worldId", worldId));

        Integer completedCount = jdbc.queryForObject("""
                SELECT COUNT(*) FROM story_sessions WHERE world_id = :worldId AND status = 'COMPLETED'""",
                new MapSqlParameterSource("worldId", worldId),Integer.class);
        Integer abandonedCount = jdbc.queryForObject("""
                SELECT COUNT(*) FROM story_sessions WHERE world_id = :worldId AND status = 'ABANDONED'""",
                new MapSqlParameterSource("worldId", worldId),Integer.class);

        jdbc.update("""
                INSERT INTO deleted_worlds
                (name, genre, description, setting_text, target_turn, rating_avg,
                 completed_count, abandoned_count, play_count, created_at, updated_at)
                VALUES (:name, :genre, :description, :settingText, :targetTurn, :ratingAvg,
                        :completedCount, :abandonedCount, :playCount, now(), now())
                """, new MapSqlParameterSource()
                .addValue("name", world.get("name"))
                .addValue("genre", world.get("genre"))
                .addValue("description", world.get("description"))
                .addValue("settingText", world.get("setting_text"))
                .addValue("targetTurn", world.get("target_turn"))
                .addValue("ratingAvg", world.get("rating_avg"))
                .addValue("completedCount", completedCount)
                .addValue("abandonedCount", abandonedCount)
                .addValue("playCount", world.get("play_count")));

        List<Long> sessionIds = jdbc.queryForList(
                "SELECT id FROM story_sessions WHERE world_id = :worldId"
                , new MapSqlParameterSource("worldId", worldId), Long.class);

        deleteSessionChildren(sessionIds);

        jdbc.update("DELETE FROM story_sessions WHERE world_id = :worldId",
                new MapSqlParameterSource("worldId", worldId));

        jdbc.update("""
                DELETE FROM persona_base_stats 
                    WHERE persona_id in (SELECT id FROM personas WHERE world_id = :worldId)
                """, new MapSqlParameterSource("worldId", worldId));

        jdbc.update("DELETE FROM personas WHERE world_id = :worldId",
                new MapSqlParameterSource("worldId", worldId));

        jdbc.update("DELETE FROM worlds WHERE id = :worldId",
                new MapSqlParameterSource("worldId", worldId));
        log.info("세계관 완전 삭제 완료 (하드 삭제) worldId : {}, 세션 {}건 삭제 완료", worldId, sessionIds.size());
    }

    /**
     * 페르소나 완전 삭제 (하드삭제)
     *
     * @param personaId 페르소나 PK
     */
    @Transactional
    public void purgePersona(Long personaId) {
        List<Long> sessionIds = jdbc.queryForList(
                "SELECT id FROM story_sessions WHERE persona_id = :personaId",
                new MapSqlParameterSource("personaId", personaId),Long.class);

        deleteSessionChildren(sessionIds);

        jdbc.update("DELETE FROM story_sessions WHERE persona_id = :personaId",
                new MapSqlParameterSource("personaId", personaId));

        jdbc.update("DELETE FROM persona_base_stats WHERE persona_id = :personaId",
                new MapSqlParameterSource("personaId", personaId));

        jdbc.update("DELETE FROM personas WHERE id = :personaId",
                new MapSqlParameterSource("personaId", personaId));

        log.info("Persona 완전 삭제 완료 personaId: {}, 세션 {}건 정리", personaId, sessionIds.size());
    }


    /* Helper Methode */

    /**
     * 세계관, 페르소나 자식 데이터 삭제 (하드삭제)
     *
     * @param sessionIds 세션 PK
     */
    private void deleteSessionChildren(List<Long> sessionIds) {
        if (sessionIds.isEmpty()) return;
        MapSqlParameterSource paramSource = new MapSqlParameterSource("ids", sessionIds);
        jdbc.update("DELETE FROM chat_messages WHERE session_id IN (:ids)", paramSource);
        jdbc.update("DELETE FROM session_items WHERE session_id IN (:ids)", paramSource);
        jdbc.update("DELETE FROM session_stat_logs WHERE session_id IN (:ids)", paramSource);
        jdbc.update("DELETE FROM session_stat_snapshots WHERE session_id IN (:ids)", paramSource);
        jdbc.update("DELETE FROM world_ratings WHERE session_id IN (:ids)", paramSource);
    }
}
