package com.worldtaletalk.world.controller;

import com.worldtaletalk.persona.dto.PersonasBaseStatsDTO;
import com.worldtaletalk.persona.dto.PersonasDTO;
import com.worldtaletalk.persona.service.PersonasBaseStatsService;
import com.worldtaletalk.persona.service.PersonasService;
import com.worldtaletalk.world.service.WorldsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/worlds")
@RequiredArgsConstructor
public class WorldViewController {

    private final WorldsService worldsService;
    private final PersonasService personasService;
    private final PersonasBaseStatsService personasBaseStatsService;

    record PersonaListItemVM(PersonasDTO persona, PersonasBaseStatsDTO baseStats) {}
}
