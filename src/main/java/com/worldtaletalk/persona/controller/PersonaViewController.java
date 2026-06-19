package com.worldtaletalk.persona.controller;

import com.worldtaletalk.session.service.StorySessionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/worlds")
@RequiredArgsConstructor
public class PersonaViewController {

    private final StorySessionsService storySessionsService;
}
