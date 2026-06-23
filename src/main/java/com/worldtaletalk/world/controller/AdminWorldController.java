package com.worldtaletalk.world.controller;

import com.worldtaletalk.world.domain.WorldGenre;
import com.worldtaletalk.world.dto.WorldsDTO;
import com.worldtaletalk.world.service.WorldsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/worlds")
@RequiredArgsConstructor
public class AdminWorldController {

    private final WorldsService worldsService;

    record WorldForm(
            @NotBlank(message = "세계관 이름을 입력해주세요")
            @Size(max = 50, message = "세계관 이름을 50자 이내로 입력해주세요.")
            String name,

            @NotNull(message = "세계관 장르를 선택해주세요.")
            WorldGenre genre,

            @NotBlank(message = "세계관 설명을 입력해주세요.")
            String description,

            @NotBlank(message = "LLM에게 주입할 세계관 프롬프트틀 입력해주세요.")
            String settingText,

            @NotNull(message = "메인 스토리 목표 턴수를 입력해주세요.")
            @Positive(message = "목표 턴수는 양수여야 합니다.")
            Integer targetTurn,

            @NotNull(message = "활성화 여부를 입력해주세요.")
            Boolean isActive
    ) {
    }

    /**
     * AW-01: 세계관 목록 조회
     *
     * @param model 뷰에 전달할 모델
     * @return admin/worlds/list (세계관 목록 페이지)
     */
    @GetMapping
    public String list(Model model) {
        List<WorldsDTO> worlds = worldsService.getAllWorlds();
        model.addAttribute("worlds", worlds);
        return "admin/worlds/list";
    }

    /**
     * AW-02: 세계관 등록 폼
     *
     * @return admin/worlds/form (세계관 등록 폼 페이지)
     */
    @GetMapping("/new")
    public String newWorld() {
        return "admin/worlds/form";
    }

    /**
     * AW-03: 세계관 수정 폼
     *
     * @param id    세계관 PK
     * @param model 뷰에 전달할 모델
     * @return admin/worlds/form (세계관 수정 폼 페이지)
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("world", worldsService.getWorldById(id));
        return "admin/worlds/form";
    }

    /**
     * AW-04: 세계관 등록
     *
     * @param form               세계관 폼
     * @param bindingResult      폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:/admin/worlds (세계관 목록 페이지)
     */
    @PostMapping
    public String create(
            @Valid @ModelAttribute WorldForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/admin/worlds/new";
        }
        worldsService.addWorld(WorldsDTO.builder()
                .name(form.name())
                .genre(form.genre())
                .description(form.description())
                .settingText(form.settingText())
                .targetTurn(form.targetTurn())
                .active(form.isActive())
                .build());
        return "redirect:/admin/worlds";
    }

    /**
     * AW-05: 세계관 수정
     *
     * @param id                 세계관 PK
     * @param form               세계관 폼
     * @param bindingResult      폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:/admin/worlds (세계관 목록 페이지)
     */
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute WorldForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/admin/worlds/" + id + "/edit";
        }

        worldsService.modifyWorld(WorldsDTO.builder()
                .id(id)
                .name(form.name())
                .genre(form.genre())
                .description(form.description())
                .settingText(form.settingText())
                .targetTurn(form.targetTurn())
                .active(form.isActive())
                .build());
        return "redirect:/admin/worlds";
    }

    /**
     * AW-06: 세계관 활성화 토글
     *
     * @param id 세계관 PK
     * @return redirect:/admin/worlds (세계관 목록 페이지)
     */
    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        WorldsDTO world = worldsService.getWorldById(id);
        worldsService.modifyWorld(WorldsDTO.builder()
                .id(world.getId())
                .active(!world.getActive())
                .build());
        return "redirect:/admin/worlds";
    }
}
