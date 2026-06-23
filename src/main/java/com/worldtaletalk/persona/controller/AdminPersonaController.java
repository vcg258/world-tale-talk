package com.worldtaletalk.persona.controller;

import com.worldtaletalk.persona.service.PersonasBaseStatsService;
import com.worldtaletalk.persona.service.PersonasService;
import com.worldtaletalk.world.service.WorldsService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
            @NotNull
            Long worldId,

            @NotBlank(message = "페르소나 이름을 입력해주세요.")
            String name,

            @NotBlank(message = "페르소나에 대한 설명을 입력해주세요.")
            String description,

            @NotBlank(message = "LLM에 주입할 페르소나 프롬프트를 입력해주세요.")
            String background,

            @NotNull(message = "페르소나 활성화 여부를 입력해주세요.")
            Boolean isActive,

            @NotNull(message = "페르소나 기본 지혜 스탯을 입력해주세요.")
            Integer wisdom,
            @NotNull(message = "페르소나 기본 힘 스탯을 입력해주세요.")
            Integer strength,
            @NotNull(message = "페르소나 기본 민첩 스탯을 입력해주세요.")
            Integer agility,
            @NotNull(message = "페르소나 기본 카리스마 스탯을 입력해주세요.")
            Integer charisma,
            @NotNull(message = "페르소나 기본 행운 스탯을 입력해주세요.")
            Integer luck
    ) {}
}
