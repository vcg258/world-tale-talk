package com.worldtaletalk.user.controller;

import com.worldtaletalk.user.service.SocialAccountsService;
import com.worldtaletalk.user.service.UserService;
import com.worldtaletalk.user.service.UserSuspensionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final UserSuspensionsService userSuspensionsService;
    private final SocialAccountsService socialAccountsService;

    record SuspendForm(String reason) {}
}
