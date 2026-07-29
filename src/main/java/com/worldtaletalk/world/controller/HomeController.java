package com.worldtaletalk.world.controller;

import com.worldtaletalk.notice.service.NoticesService;
import com.worldtaletalk.world.service.WorldsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
    private final WorldsService worldsService;
    private final NoticesService noticesService;


    /**
     * H-01: 메인 화면 조회
     * @param model 뷰에 전달할 모델
     * @return home/index (메인 페이지)
     */
    @GetMapping
    public String home(Model model) {
        model.addAttribute("worlds", worldsService.getActiveWorlds());
        model.addAttribute("featuredWorlds", worldsService.getFeaturedWorlds(3));
        model.addAttribute("notices", noticesService.getLatestActiveNotices(5));
        return "home/index";
    }

    /**
     * H-02: 세계관 목록 화면 조회
     *
     * @param model 뷰에 전달할 모델
     * @return world/list (세계관 목록 페이지)
     */
    @GetMapping("/worlds")
    public String worlds(Model model) {
        model.addAttribute("worlds", worldsService.getActiveWorlds());
        return "world/list";
    }
}
