package com.worldtaletalk.notice.controller;

import com.worldtaletalk.notice.service.NoticesService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticesService noticesService;

    record NoticeForm(
            @NotBlank(message = "공지 제목을 입력해주세요.")
            @Size(max = 50, message = "제목은 최대 50자 이내로 입력해주세요.")
            String title,

            @NotBlank(message = "공지 내용을 입력해주세요.")
            String content,

            @NotNull(message = "활성화 여부를 입력해주세요.")
            Boolean isActive
    ) {}
}
