package com.worldtaletalk.persona.controller;

import com.worldtaletalk.global.security.CustomUserDetails;
import com.worldtaletalk.session.service.StorySessionsService;
import com.worldtaletalk.user.domain.user.UserRole;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonaViewController.class)
class PersonaViewControllerTest {

    private static final String CREATE_SESSION = "/worlds/{worldId}/personas/{personaId}/sessions";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private StorySessionsService storySessionsService;
    @MockitoBean private UserService userService;
    private CustomUserDetails loginUser;

    @BeforeEach
    void setUp() {
        loginUser = new CustomUserDetails(
                UserDTO.builder().id(1L).email("user@test.com").role(UserRole.USER).status(UserStatus.ACTIVE).build()
        );
        given(userService.getUser(any())).willReturn(
                UserDTO.builder().id(1L).role(UserRole.USER).status(UserStatus.ACTIVE).build()
        );
    }

    @Test
    void createSession() throws Exception {
        given(storySessionsService.createStorySession(any())).willReturn(1L);

        mockMvc.perform(post(CREATE_SESSION, 1L, 1L)
                        .with(csrf())
                        .with(user(loginUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions/1/play"))
                .andDo(print());

        then(storySessionsService).should().createStorySession(any());
    }
}
