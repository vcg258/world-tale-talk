package com.worldtaletalk.persona.service;

import com.worldtaletalk.persona.dto.PersonasDTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Log4j2
@SpringBootTest
class PersonasServiceImplTest {
    @Autowired private PersonasService personasService;

    @Test
    void getPersonasList() {
        personasService.getPersonasList().forEach(log::info);
    }

    @Test
    void getPersona() {
        log.info(personasService.getPersona(1L));
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
        personasService.addPersona(personasDTO);
    }
}