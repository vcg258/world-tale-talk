package com.worldtaletalk.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    // 이 옵션을 사용하게 된 이유 삭제를 하는 건수가 다건이 될 경우 다건중 하나라도 오류가 발생할경우 다른 건까지도 롤백되는걸 방지하기 위함
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void purgeWorld(Long worldId) {
        // 해당 세계관 조회
        Map<String, Object> world = jdbc.queryForMap("""
                SELECT * FROM worlds WHERE id = :worldId
                """, new MapSqlParameterSource("worldId", worldId));

        // 세계관 완결낸 세션 개수 조회
        Integer completedCount = jdbc.queryForObject("""
                SELECT COUNT(*) FROM story_sessions WHERE world_id = :worldId AND status = 'COMPLETED'""",
                new MapSqlParameterSource("worldId", worldId),Integer.class);
        // 세계관 중도포기한 세션 개수 조회
        Integer abandonedCount = jdbc.queryForObject("""
                SELECT COUNT(*) FROM story_sessions WHERE world_id = :worldId AND status = 'ABANDONED'""",
                new MapSqlParameterSource("worldId", worldId),Integer.class);

        // 통계를 위한 세계관 삭제 데이터 저장
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

        // 해당 세계관 세션 조회
        List<Long> sessionIds = jdbc.queryForList(
                "SELECT id FROM story_sessions WHERE world_id = :worldId",
                 new MapSqlParameterSource("worldId", worldId), Long.class);

        // 자식 데이터 차례대로 삭제
        deleteSessionChildren(sessionIds);

        // 해당 세계관 세션 삭제
        jdbc.update("DELETE FROM story_sessions WHERE world_id = :worldId",
                new MapSqlParameterSource("worldId", worldId));

        // 해당 세계관 페르소나 기본 스탯 삭제
        jdbc.update("""
                DELETE FROM persona_base_stats 
                    WHERE persona_id in (SELECT id FROM personas WHERE world_id = :worldId)
                """, new MapSqlParameterSource("worldId", worldId));

        // 해당 세계관 페르소나 삭제
        jdbc.update("DELETE FROM personas WHERE world_id = :worldId",
                new MapSqlParameterSource("worldId", worldId));

        // 해당 세계관 삭제
        jdbc.update("DELETE FROM worlds WHERE id = :worldId",
                new MapSqlParameterSource("worldId", worldId));
        log.info("세계관 완전 삭제 완료 (하드 삭제) worldId : {}, 세션 {}건 삭제 완료", worldId, sessionIds.size());
    }

    /**
     * 페르소나 완전 삭제 (하드삭제)
     *
     * @param personaId 페르소나 PK
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void purgePersona(Long personaId) {
        // 해당 페르소나를 선택한 세션PK 조회
        List<Long> sessionIds = jdbc.queryForList(
                "SELECT id FROM story_sessions WHERE persona_id = :personaId",
                new MapSqlParameterSource("personaId", personaId),Long.class);

        // 자식 데이터 차례대로 삭제
        deleteSessionChildren(sessionIds);

        // 해당 페르소나를 선택한 세션 삭제
        jdbc.update("DELETE FROM story_sessions WHERE persona_id = :personaId",
                new MapSqlParameterSource("personaId", personaId));

        // 해당 페르소나 기본 스탯 삭제
        jdbc.update("DELETE FROM persona_base_stats WHERE persona_id = :personaId",
                new MapSqlParameterSource("personaId", personaId));

        // 페르소나 삭제
        jdbc.update("DELETE FROM personas WHERE id = :personaId",
                new MapSqlParameterSource("personaId", personaId));

        log.info("Persona 완전 삭제 완료 personaId: {}, 세션 {}건 정리", personaId, sessionIds.size());
    }

    /**
     * 삭제 대기시간이 된 세계관 조회
     *
     * @param cutoff 기준시간 전 시간
     * @return 삭제 대기시간 만료 세계관
     */
    @Transactional(readOnly = true)
    public List<Long> findExpiredWorldIds(LocalDateTime cutoff) {
        return jdbc.queryForList("SELECT id FROM worlds WHERE deleted_at IS NOT NULL AND deleted_at <= :cutoff",
                new MapSqlParameterSource("cutoff", cutoff), Long.class);
    }

    /**
     * 삭제 대기시간이 된 페르소나
     *
     * @param cutoff 기준시간 전 시간
     * @return 삭제 대기사간 만료 페르소나
     */
    @Transactional(readOnly = true)
    public List<Long> findExpiredPersonaIds(LocalDateTime cutoff) {
        return jdbc.queryForList("SELECT id FROM personas WHERE deleted_at IS NOT NULL AND deleted_at <= :cutoff",
                new MapSqlParameterSource("cutoff", cutoff), Long.class);
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

        log.info("자식 데이터 차례대로 삭제 완료");
    }
}
