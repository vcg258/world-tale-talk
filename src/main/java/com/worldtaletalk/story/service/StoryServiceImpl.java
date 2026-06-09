package com.worldtaletalk.story.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {
    @Override
    public void processStory(Long sessionId, String userInput) {

    }
}
