package com.worldtaletalk.rating.controller;

import com.worldtaletalk.rating.dto.WorldRatingsDTO;
import com.worldtaletalk.rating.service.WorldRatingsService;
import com.worldtaletalk.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminRatingController.class)
class AdminRatingControllerTest {

    private static final String LIST = "/admin/ratings";
    private static final String DELETE = "/admin/ratings/{id}";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private WorldRatingsService worldRatingsService;
    @MockitoBean private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void list() throws Exception {
        Page<WorldRatingsDTO> dto = new PageImpl<>(List.of(WorldRatingsDTO.builder().id(1L).build()));
        given(worldRatingsService.getWorldRatings(any(), any(), anyInt())).willReturn(dto);

        mockMvc.perform(get(LIST))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/ratings/list"))
                .andExpect(model().attribute("ratings", dto.getContent()))
                .andExpect(model().attribute("ratingsPage", dto))
                .andDo(print());

        then(worldRatingsService).should().getWorldRatings(null, null, 1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete() throws Exception {
        mockMvc.perform(post(DELETE, 1L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LIST))
                .andDo(print());

        then(worldRatingsService).should().deleteWorldRating(eq(1L));
    }
}
