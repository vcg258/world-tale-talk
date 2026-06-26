package com.worldtaletalk.persona.controller;

import com.worldtaletalk.persona.dto.PersonasBaseStatsDTO;
import com.worldtaletalk.persona.dto.PersonasDTO;
import com.worldtaletalk.persona.service.PersonasBaseStatsService;
import com.worldtaletalk.persona.service.PersonasService;
import com.worldtaletalk.world.service.WorldsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/personas")
@RequiredArgsConstructor
public class AdminPersonaController {

    private final PersonasService personasService;
    private final PersonasBaseStatsService personasBaseStatsService;
    private final WorldsService worldsService;

    // 페르소나 등록·수정 요청 폼 - 세계관 ID, 이름, 설명, 배경, 스탯 등 전체 필드를 하나의 레코드로 처리함
    private record PersonaForm(
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
    ) {
    }

    /**
     * AP-01: 페르소나 목록 조회
     *
     * @param worldId worldId 세계관 PK 필터 (null이면 전체 조회)
     * @param model   뷰에 전달할 모델
     * @return admin/personas/list (페르소나 목록 페이지)
     */
    @GetMapping
    public String list(@RequestParam(required = false) Long worldId, Model model) {
        List<PersonasDTO> personas = worldId != null ?
                personasService.getPersonasAllByWorldId(worldId) : personasService.getPersonasList();
        model.addAttribute("personas", personas);
        model.addAttribute("worlds", worldsService.getAllWorlds());
        model.addAttribute("worldId", worldId);
        return "admin/personas/list";
    }

    /**
     * AP-02: 페르소나 등록 폼
     *
     * @param model 뷰에 전달할 모델
     * @return admin/personas/form (페르소나 등록 폼 페이지)
     */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("worlds", worldsService.getAllWorlds());
        return "admin/personas/form";
    }

    /**
     * AP-03: 페르소나 수정 폼
     *
     * @param id    페르소나 PK
     * @param model 뷰에 전달할 모델
     * @return admin/personas/form (페르소나 수정 폼 페이지)
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PersonasDTO personas = personasService.getPersona(id);
        model.addAttribute("persona", personas);
        model.addAttribute("baseStats", personasBaseStatsService.getPersonasBaseStatsByPersonaId(personas.getId()));
        model.addAttribute("worlds", worldsService.getAllWorlds());
        return "admin/personas/form";
    }

    /**
     * AP-04: 페르소나 등록
     *
     * @param form               페르소나 폼
     * @param bindingResult      폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:/admin/personas (페르소나 목록 페이지)
     */
    @PostMapping
    public String create(
            @Valid @ModelAttribute PersonaForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/admin/personas/new";
        }
        Long persona = personasService.addPersona(PersonasDTO.builder()
                .worldId(form.worldId())
                .name(form.name())
                .description(form.description())
                .background(form.background())
                .active(form.isActive())
                .build());
        personasBaseStatsService.addPersonaBaseStats(PersonasBaseStatsDTO.builder()
                .personaId(persona)
                .wisdom(form.wisdom())
                .strength(form.strength())
                .agility(form.agility())
                .charisma(form.charisma())
                .luck(form.luck())
                .build());
        return "redirect:/admin/personas";
    }
}
