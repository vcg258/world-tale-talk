package com.worldtaletalk.rating.controller;

import com.worldtaletalk.global.security.CustomUserDetails;
import com.worldtaletalk.rating.dto.WorldRatingsDTO;
import com.worldtaletalk.rating.service.WorldRatingsService;
import com.worldtaletalk.user.domain.user.UserRole;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import com.worldtaletalk.world.dto.WorldsDTO;
import com.worldtaletalk.world.service.WorldsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RatingViewController.class)
class RatingViewControllerTest {

    private static final String FORM = "/worlds/{worldId}/ratings";
    private static final String EDIT_FORM = "/worlds/{worldId}/ratings/edit";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private WorldRatingsService worldRatingsService;
    @MockitoBean private WorldsService worldsService;
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
        given(worldsService.getWorldById(1L)).willReturn(WorldsDTO.builder().id(1L).build());
    }

    @Test
    void ratingForm() throws Exception {
        given(worldRatingsService.getMyRatingByWorldAndUser(eq(1L), eq(1L)))
                .willReturn(WorldRatingsDTO.builder().id(1L).build());

        mockMvc.perform(get(FORM, 1L)
                        .param("sessionId", "1")
                        .with(user(loginUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("rating/form"))
                .andExpect(model().attributeExists("world"))
                .andExpect(model().attributeExists("sessionId"))
                .andExpect(model().attributeExists("existingRating"))
                .andDo(print());

        then(worldRatingsService).should().getMyRatingByWorldAndUser(eq(1L), eq(1L));
    }

    @Test
    void ratingEditForm_exists() throws Exception {
        given(worldRatingsService.getMyRatingByWorldAndUser(eq(1L), eq(1L)))
                .willReturn(WorldRatingsDTO.builder().id(1L).storySessionId(1L).build());

        mockMvc.perform(get(EDIT_FORM, 1L).with(user(loginUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("rating/form"))
                .andExpect(model().attributeExists("world"))
                .andExpect(model().attributeExists("existingRating"))
                .andDo(print());
    }

    @Test
    void ratingEditForm_notFound() throws Exception {
        given(worldRatingsService.getMyRatingByWorldAndUser(eq(1L), eq(1L))).willReturn(null);

        mockMvc.perform(get(EDIT_FORM, 1L).with(user(loginUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage/ratings"))
                .andDo(print());
    }

    @Test
    void saveRating_new() throws Exception {
        given(worldRatingsService.getMyRatingByWorldAndUser(eq(1L), eq(1L))).willReturn(null);

        mockMvc.perform(post(FORM, 1L)
                        .param("sessionId", "1")
                        .param("score", "5")
                        .param("review", "좋아요")
                        .with(csrf())
                        .with(user(loginUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage/ratings"))
                .andDo(print());

        then(worldRatingsService).should().addWorldRating(any());
        then(worldRatingsService).should(never()).modifyWorldRating(any());
    }

    @Test
    void saveRating_invalid() throws Exception {
        mockMvc.perform(post(FORM, 1L)
                        .param("sessionId", "1")
                        .param("score", "0")
                        .param("review", "리뷰")
                        .with(csrf())
                        .with(user(loginUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/worlds/1/ratings?sessionId=1"))
                .andDo(print());

        then(worldRatingsService).should(never()).addWorldRating(any());
        then(worldRatingsService).should(never()).modifyWorldRating(any());
    }
}
