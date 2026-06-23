package com.worldtaletalk.item.controller;

import com.worldtaletalk.item.service.ItemGradeConfigsService;
import com.worldtaletalk.item.service.ItemGradeProbabilitiesService;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/items")
@RequiredArgsConstructor
public class AdminItemController {

    private final ItemGradeConfigsService itemGradeConfigsService;
    private final ItemGradeProbabilitiesService itemGradeProbabilitiesService;

    record GradeConfigForm(
            @NotNull(message = "스탯 합산 최대치를 입력해주세요.")
            @Positive(message = "스탯 합산 최대치는 양수여야 합니다.")
            Integer statSumMax,

            @NotNull(message = "단일 스탯 최대치를 입력해주세요.")
            @Positive(message = "단일 스탯 최대치는 양수여야 합니다.")
            Integer singleMax,

            @NotNull(message = "단일 스탯 최소치를 입력해주세요.")
            Integer singleMin,

            @NotNull(message = "음수 보정치 허용 여부를 입력해주세요.")
            Boolean canNegative) {}

    record ProbabilityForm(
            @NotNull(message = "확률을 입력해주세요.")
            @Min(value = 0, message = "확률은 0 이상이어야 합니다.")
            @Max(value = 100, message = "확률은 100 이하여야 합니다.")
            Integer probability
    ) {}
}
