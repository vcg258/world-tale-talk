package com.worldtaletalk.notice.controller;

import com.worldtaletalk.notice.service.NoticesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticesService noticesService;

    record NoticeForm(String title, String content, boolean isActive) {}
}
