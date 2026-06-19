package com.worldtaletalk.world.controller;

import com.worldtaletalk.world.service.WorldsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/worlds")
@RequiredArgsConstructor
public class AdminWorldController {

    private final WorldsService worldsService;

    record WorldForm(
            String name,
            String genre,
            String description,
            String settingText,
            int targetTurn,
            boolean isActive
    ) {}
}
