package com.worldtaletalk.world.controller;

import com.worldtaletalk.world.domain.WorldGenre;
import com.worldtaletalk.world.service.WorldsService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
    ) {}
}
