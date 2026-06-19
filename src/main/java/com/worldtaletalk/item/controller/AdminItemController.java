package com.worldtaletalk.item.controller;

import com.worldtaletalk.item.service.ItemGradeConfigsService;
import com.worldtaletalk.item.service.ItemGradeProbabilitiesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/items")
@RequiredArgsConstructor
public class AdminItemController {

    private final ItemGradeConfigsService itemGradeConfigsService;
    private final ItemGradeProbabilitiesService itemGradeProbabilitiesService;

    record GradeConfigForm(int statSumMax, int singleMax, int singleMin, boolean canNegative) {}

    record ProbabilityForm(int probability) {}
}
