package com.worldtaletalk.persona.repository;

import com.worldtaletalk.persona.domain.PersonasBaseStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonasBaseStatsRepository extends JpaRepository<PersonasBaseStatsEntity, Long> {
    // FK 페르소나 아이디로 조회
    Optional<PersonasBaseStatsEntity> findByPersonaId(Long personaId);
}
