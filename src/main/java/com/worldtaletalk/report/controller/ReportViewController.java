package com.worldtaletalk.report.controller;

import com.worldtaletalk.report.domain.ReportTargetType;
import com.worldtaletalk.report.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportViewController {

    private final ReportsService reportsService;

    record ReportForm(ReportTargetType targetType, Long targetId, String reason) {}
}
