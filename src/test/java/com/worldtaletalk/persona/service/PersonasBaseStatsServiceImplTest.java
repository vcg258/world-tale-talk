package com.worldtaletalk.persona.service;

import com.worldtaletalk.persona.dto.PersonasBaseStatsDTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@Log4j2
@SpringBootTest
class PersonasBaseStatsServiceImplTest {
    @Autowired PersonasBaseStatsService personasBaseStatsService;

    @Test
    void getPersonasBaseStatsList() {
        personasBaseStatsService.getPersonasBaseStatsList().forEach(log::info);
    }

    @Test
    void getPersonasBaseStats() {
        log.info(personasBaseStatsService.getPersonasBaseStats(1L));
    }

    @Test
    void addPersonaBaseStats() {
        PersonasBaseStatsDTO baseStatsDTO = PersonasBaseStatsDTO.builder()
                .personaId(5L)
                .wisdom(8)
                .strength(8)
                .agility(8)
                .charisma(8)
                .luck(8)
                .build();
        personasBaseStatsService.addPersonaBaseStats(baseStatsDTO);
    }

}