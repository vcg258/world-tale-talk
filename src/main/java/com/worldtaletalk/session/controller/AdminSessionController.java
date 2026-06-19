package com.worldtaletalk.session.controller;

import com.worldtaletalk.session.service.StorySessionsService;
import com.worldtaletalk.world.service.WorldsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/sessions")
@RequiredArgsConstructor
public class AdminSessionController {

    private final StorySessionsService storySessionsService;
    private final WorldsService worldsService;
}
