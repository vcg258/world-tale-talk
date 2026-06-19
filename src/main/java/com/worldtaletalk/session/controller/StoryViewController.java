package com.worldtaletalk.session.controller;

import com.worldtaletalk.chat.service.ChatMessagesService;
import com.worldtaletalk.inventory.service.SessionItemsService;
import com.worldtaletalk.persona.service.PersonasService;
import com.worldtaletalk.session.service.StorySessionsService;
import com.worldtaletalk.stat.service.SessionStatsService;
import com.worldtaletalk.world.service.WorldsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class StoryViewController {

    private final StorySessionsService storySessionsService;
    private final WorldsService worldsService;
    private final PersonasService personasService;
    private final ChatMessagesService chatMessagesService;
    private final SessionItemsService sessionItemsService;
    private final SessionStatsService sessionStatsService;

    record CurrentStatsVM(
            int wisdom,
            int strength,
            int agility,
            int charisma,
            int luck
    ) {}
}
