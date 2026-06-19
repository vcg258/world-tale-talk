package com.worldtaletalk.rating.controller;

import com.worldtaletalk.rating.service.WorldRatingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/ratings")
@RequiredArgsConstructor
public class AdminRatingController {

    private final WorldRatingsService worldRatingsService;
}
