package com.worldtaletalk.persona.repository;

import com.worldtaletalk.persona.domain.PersonasEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonasRepository extends JpaRepository<PersonasEntity, Long> {
    // 해당 세계관에 활성화 된 페르소나 전체 조회
    List<PersonasEntity> findAllByWorldIdAndActiveTrue(Long id);

    // 해당 세계관 페르소나 전체 조회 (비활성 포함, 관리자용)
    List<PersonasEntity> findAllByWorldId(Long worldId);
}
