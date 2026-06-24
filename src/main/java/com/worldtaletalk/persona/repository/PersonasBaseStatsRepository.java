package com.worldtaletalk.persona.repository;

import com.worldtaletalk.persona.domain.PersonasBaseStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PersonasBaseStatsRepository extends JpaRepository<PersonasBaseStatsEntity, Long> {
    // FK 페르소나 아이디로 조회
    Optional<PersonasBaseStatsEntity> findByPersonaId(Long personaId);

    // 페르소나 기본 스탯과 스토리 세션 서브쿼리로 조회
    @Query("""
        SELECT p FROM PersonasBaseStatsEntity p
            WHERE p.persona.id =
                (SELECT s.personas.id FROM StorySessionsEntity s WHERE s.id = :sessionId)
    """)
    Optional<PersonasBaseStatsEntity> findBySessionId(@Param("sessionId") Long sessionId);


}
