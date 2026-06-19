package com.worldtaletalk.rating.controller;

import com.worldtaletalk.rating.service.WorldRatingsService;
import com.worldtaletalk.world.service.WorldsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/worlds")
@RequiredArgsConstructor
public class RatingViewController {

    private final WorldRatingsService worldRatingsService;
    private final WorldsService worldsService;

    record RatingForm(Long sessionId, short score, String review) {}
}
