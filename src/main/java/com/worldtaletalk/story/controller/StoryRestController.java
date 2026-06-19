package com.worldtaletalk.story.controller;

import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.story.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class StoryRestController {

    private final StoryService storyService;

    record TurnRequest(String content, Integer choiceIndex) {}

    record TurnResponse(
            int turn,
            String story,
            List<String> choices,
            Map<String, Integer> statChanges,
            boolean endingTriggered,
            SessionStatus sessionStatus
    ) {}
}
