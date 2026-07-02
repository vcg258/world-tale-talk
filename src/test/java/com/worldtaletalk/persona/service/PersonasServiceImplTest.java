package com.worldtaletalk.persona.service;

import com.worldtaletalk.persona.dto.PersonasDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class PersonasServiceImplTest {
    @Autowired private PersonasService personasService;

    @Test
    void getPersonasList() {
        personasService.getPersonasList().forEach(dto -> log.info("{}", dto));
    }

    @Test
    void getPersona() {
        log.info("{}", personasService.getPersona(1L));
    }

    @Test
    void getPersonasByWorldIdWithBaseStats() {
        personasService.getPersonasByWorldIdWithBaseStats(1L).forEach(dto -> log.info("{}", dto));
    }

    @Test
    void addPersona() {
        PersonasDTO personasDTO = PersonasDTO.builder()
                .worldId(1L)
                .name("name")
                .description("description")
                .background("background")
                .active(true)
                .build();
        log.info("{}", personasService.addPersona(personasDTO));
    }

    @Test
    void modifyPersona() {
        PersonasDTO personasDTO = PersonasDTO.builder()
                .id(1L)
                .worldId(2L)
                .active(true)
                .build();
        personasService.modifyPersona(personasDTO);
    }

    @Test
    void deletePersona() {
        personasService.deletePersona(5L);
    }

    @Test
    void getPersonasByWorldId() {
        personasService.getPersonasByWorldId(1L).forEach(dto -> log.info("{}", dto));
    }
}