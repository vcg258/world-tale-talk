package com.worldtaletalk.persona.repository;

import com.worldtaletalk.persona.domain.PersonasEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonasRepository extends JpaRepository<PersonasEntity, Long> {
}
