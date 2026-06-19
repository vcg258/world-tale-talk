package com.worldtaletalk.persona.controller;

import com.worldtaletalk.persona.service.PersonasBaseStatsService;
import com.worldtaletalk.persona.service.PersonasService;
import com.worldtaletalk.world.service.WorldsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/personas")
@RequiredArgsConstructor
public class AdminPersonaController {

    private final PersonasService personasService;
    private final PersonasBaseStatsService personasBaseStatsService;
    private final WorldsService worldsService;

    record PersonaForm(
            Long worldId,
            String name,
            String description,
            String background,
            boolean isActive,
            int wisdom,
            int strength,
            int agility,
            int charisma,
            int luck
    ) {}
}
