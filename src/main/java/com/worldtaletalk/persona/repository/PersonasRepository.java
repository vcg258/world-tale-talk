package com.worldtaletalk.persona.repository;

import com.worldtaletalk.persona.domain.PersonasEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PersonasRepository extends JpaRepository<PersonasEntity, Long> {
    // 해당 세계관에 활성화 된 페르소나 전체 조회
    List<PersonasEntity> findAllByWorldIdAndActiveTrue(Long id);

    // 해당 세계관 페르소나 전체 조회 (비활성 포함, 관리자용)
    List<PersonasEntity> findAllByWorldId(Long worldId);

    // 해당 세계관 페르소나 + 기본 스탯 전체 조회
    @Query("SELECT p FROM PersonasEntity p LEFT JOIN FETCH p.personaBaseStats WHERE p.world.id = :worldId")
    List<PersonasEntity> findAllByWorldIdWithBaseStats(@Param("worldId") Long worldId);
}
