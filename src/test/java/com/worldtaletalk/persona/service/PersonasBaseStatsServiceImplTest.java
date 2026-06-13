package com.worldtaletalk.persona.service;

import com.worldtaletalk.persona.dto.PersonasBaseStatsDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@Slf4j
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
    void getPersonasBaseStatsByPersonaId() {
        log.info(personasBaseStatsService.getPersonasBaseStatsByPersonaId(1L));
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

    @Test
    void modifyPersonaBaseStats() {
        PersonasBaseStatsDTO baseStatsDTO = PersonasBaseStatsDTO.builder()
                .id(7L)
                .wisdom(10)
                .strength(10)
                .agility(10)
                .charisma(10)
                .luck(10)
                .build();
        personasBaseStatsService.modifyPersonaBaseStats(baseStatsDTO);
    }

}