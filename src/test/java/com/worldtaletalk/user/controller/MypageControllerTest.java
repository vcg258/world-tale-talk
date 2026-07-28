package com.worldtaletalk.user.controller;

import com.worldtaletalk.global.security.CustomUserDetails;
import com.worldtaletalk.rating.dto.WorldRatingsDTO;
import com.worldtaletalk.rating.service.WorldRatingsService;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.session.service.StorySessionsService;
import com.worldtaletalk.user.domain.user.UserRole;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MypageController.class)
class MypageControllerTest {

    private static final String INDEX = "/mypage";
    private static final String PROFILE = "/mypage/profile";
    private static final String WITHDRAW = "/mypage/withdraw";
    private static final String SESSIONS = "/mypage/sessions";
    private static final String RATINGS = "/mypage/ratings";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    @MockitoBean private StorySessionsService storySessionsService;
    @MockitoBean private WorldRatingsService worldRatingsService;
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
    void profile() throws Exception {
        mockMvc.perform(get(PROFILE).with(user(loginUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/profile"))
                .andExpect(model().attributeExists("user"))
                .andDo(print());
    }

    @Test
    void updateProfile() throws Exception {
        mockMvc.perform(post(PROFILE)
                        .param("nickname", "newNick")
                        .with(csrf())
                        .with(user(loginUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(PROFILE))
                .andDo(print());

        then(userService).should().modifyNickname(eq(1L), eq("newNick"));
    }

    @Test
    void updateProfile_invalid() throws Exception {
        mockMvc.perform(post(PROFILE)
                        .param("nickname", "")
                        .with(csrf())
                        .with(user(loginUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(PROFILE))
                .andDo(print());

        then(userService).should(never()).modifyNickname(any(), any());
    }

    @Test
    void withdraw() throws Exception {
        mockMvc.perform(post(WITHDRAW)
                        .with(csrf())
                        .with(user(loginUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/logout"))
                .andDo(print());

        then(userService).should().deleteUser(eq(1L));
    }

    @Test
    void sessions() throws Exception {
        given(storySessionsService.getMySessions(eq(1L), isNull(), eq(1)))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get(SESSIONS).with(user(loginUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/sessions"))
                .andExpect(model().attributeExists("sessions"))
                .andExpect(model().attributeExists("sessionPage"))
                .andDo(print());

        then(storySessionsService).should().getMySessions(eq(1L), isNull(), eq(1));
    }

    @Test
    void ratings() throws Exception {
        given(worldRatingsService.getMyRatings(eq(1L), eq(1)))
                .willReturn(new PageImpl<>(List.of(WorldRatingsDTO.builder().id(1L).build())));

        mockMvc.perform(get(RATINGS).with(user(loginUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/ratings"))
                .andExpect(model().attributeExists("ratings"))
                .andExpect(model().attributeExists("ratingsPage"))
                .andDo(print());

        then(worldRatingsService).should().getMyRatings(eq(1L), eq(1));
    }
}
