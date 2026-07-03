package com.worldtaletalk.item.controller;

import com.worldtaletalk.global.util.BindingResultUtils;
import com.worldtaletalk.item.domain.itemgradeprobabilities.PhaseType;
import com.worldtaletalk.item.dto.ItemGradeConfigsDTO;
import com.worldtaletalk.item.dto.ItemGradeProbabilitiesDTO;
import com.worldtaletalk.item.service.ItemGradeConfigsService;
import com.worldtaletalk.item.service.ItemGradeProbabilitiesService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/items")
@RequiredArgsConstructor
public class AdminItemController {

    private final ItemGradeConfigsService itemGradeConfigsService;
    private final ItemGradeProbabilitiesService itemGradeProbabilitiesService;

    // 아이템 등급 스탯 설정 요청 폼 - 합산 최대치, 단일 최대치, 최소치, 음수 허용 여부 네 필드를 하나의 레코드로 처리함
    private record GradeConfigForm(
            @NotNull(message = "스탯 합산 최대치를 입력해주세요.")
            @Positive(message = "스탯 합산 최대치는 양수여야 합니다.")
            Integer statSumMax,

            @NotNull(message = "단일 스탯 최대치를 입력해주세요.")
            @Positive(message = "단일 스탯 최대치는 양수여야 합니다.")
            Integer singleMax,

            @NotNull(message = "단일 스탯 최소치를 입력해주세요.")
            Integer singleMin,

            @NotNull(message = "음수 보정치 허용 여부를 입력해주세요.")
            Boolean canNegative) {
    }

    // 아이템 등급 확률 수정 요청 폼 - 확률 단일 필드를 레코드로 처리함
    private record ProbabilityForm(
            @NotNull(message = "확률을 입력해주세요.")
            @Min(value = 0, message = "확률은 0 이상이어야 합니다.")
            @Max(value = 100, message = "확률은 100 이하여야 합니다.")
            Integer probability
    ) {
    }

    /**
     * AI-01: 아이템 등급 설정 목록 조회
     *
     * @param model 뷰에 전달할 모델
     * @return admin/items/grades (아이템 등급 설정 페이지)
     */
    @GetMapping("/grades")
    public String grades(Model model) {
        model.addAttribute("gradeConfigs", itemGradeConfigsService.getGradeConfigs());
        return "admin/items/grades";
    }

    /**
     * AI-02: 아이템 등급 설정 수정
     *
     * @param id                 등급 설정 PK
     * @param form               등급 설정 폼
     * @param bindingResult      폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:/admin/items/grades (아이템 등급 설정 페이지)
     */
    @PostMapping("/grades/{id}")
    public String updateGrade(@PathVariable Long id,
                              @Valid @ModelAttribute GradeConfigForm form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    BindingResultUtils.getFirstErrorMessage(bindingResult, "입력값을 확인해주세요."));
            return "redirect:/admin/items/grades";
        }
        itemGradeConfigsService.modifyGradeConfig(id, ItemGradeConfigsDTO.builder()
                .statSumMax(form.statSumMax())
                .singleMax(form.singleMax())
                .singleMin(form.singleMin())
                .canNegative(form.canNegative())
                .build());
        return "redirect:/admin/items/grades";
    }

    /**
     * AI-03: 아이템 등급 확률 목록 조회
     *
     * @param model 뷰에 전달할 모델
     * @return admin/items/probabilities (아이템 등급 확률 페이지)
     */
    @GetMapping("/probabilities")
    public String probabilities(Model model) {
        Map<PhaseType, List<ItemGradeProbabilitiesDTO>> probabilitiesByPhase = Arrays.stream(PhaseType.values())
                .collect(Collectors.toMap(
                        phase -> phase,
                        itemGradeProbabilitiesService::getProbabilitiesByPhase));
        model.addAttribute("phases", PhaseType.values());
        model.addAttribute("probabilitiesByPhase", probabilitiesByPhase);
        return "admin/items/probabilities";
    }

    /**
     * AI-04: 아이템 등급 확률 수정
     *
     * @param id                 확률 설정 PK
     * @param form               확률 폼
     * @param bindingResult      폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:/admin/items/probabilities (아이템 등급 확률 페이지)
     */
    @PostMapping("/probabilities/{id}")
    public String updateProbability(
            @PathVariable Long id,
            @Valid @ModelAttribute ProbabilityForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    BindingResultUtils.getFirstErrorMessage(bindingResult, "입력값을 확인해주세요."));
            return "redirect:/admin/items/probabilities";
        }
        itemGradeProbabilitiesService.modifyProbability(id, form.probability());
        return "redirect:/admin/items/probabilities";
    }
}
