package com.worldtaletalk.world.controller;

import com.worldtaletalk.persona.dto.PersonasBaseStatsDTO;
import com.worldtaletalk.persona.dto.PersonasDTO;
import com.worldtaletalk.persona.service.PersonasService;
import com.worldtaletalk.world.service.WorldsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/worlds")
@RequiredArgsConstructor
public class WorldViewController {

    private final WorldsService worldsService;
    private final PersonasService personasService;

    // 세계관 상세 페르소나 목록 뷰 모델 - 페르소나 정보와 기본 스탯을 묶어 Thymeleaf에 전달하는 레코드
    private record PersonaListItemVM(PersonasDTO persona, PersonasBaseStatsDTO baseStats) {}

    /**
     * W-01: 세계관 상세 조회
     *
     * @param id    세계관 PK
     * @param model 뷰에 전달할 모델
     * @return world/detail (세계관 상세 페이지)
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("world", worldsService.getWorldById(id));
        return "world/detail";
    }

    /**
     * W-02: 세계관 페르소나 목록 조회
     *
     * @param id    세계관 PK
     * @param model 뷰에 전달할 모델
     * @return persona/list (페르소나 목록 페이지)
     */
    @GetMapping("/{id}/personas")
    public String personas(@PathVariable Long id, Model model) {
        List<PersonaListItemVM> personas = personasService.getPersonasByWorldIdWithBaseStats(id).stream()
                .map(persona -> new PersonaListItemVM(persona, persona.getBaseStats())).toList();
        model.addAttribute("world", worldsService.getWorldById(id));
        model.addAttribute("personas", personas);
        return "persona/list";
    }
}
