package com.worldtaletalk.user.controller;

import com.worldtaletalk.rating.service.WorldRatingsService;
import com.worldtaletalk.session.service.StorySessionsService;
import com.worldtaletalk.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final UserService userService;
    private final StorySessionsService storySessionsService;
    private final WorldRatingsService worldRatingsService;

    record ProfileForm(String nickname) {}
}
