package com.worldtaletalk.session.controller;

import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.session.service.StorySessionsService;
import com.worldtaletalk.user.service.UserService;
import com.worldtaletalk.world.dto.WorldsDTO;
import com.worldtaletalk.world.service.WorldsService;
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

@WebMvcTest(AdminSessionController.class)
class AdminSessionControllerTest {

    private static final String LIST = "/admin/sessions";
    private static final String ABANDON = "/admin/sessions/{id}/abandon";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private StorySessionsService storySessionsService;
    @MockitoBean private WorldsService worldsService;
    @MockitoBean private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void list() throws Exception {
        Page<StorySessionsDTO> dto = new PageImpl<>(List.of(StorySessionsDTO.builder().id(1L).build()));
        List<WorldsDTO> worlds = List.of(WorldsDTO.builder().id(1L).build());
        given(storySessionsService.getSessions(any(), any(), any(), anyInt())).willReturn(dto);
        given(worldsService.getAllWorlds()).willReturn(worlds);

        mockMvc.perform(get(LIST))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/sessions/list"))
                .andExpect(model().attribute("sessions", dto.getContent()))
                .andExpect(model().attribute("sessionsPage", dto))
                .andExpect(model().attribute("worlds", worlds))
                .andDo(print());

        then(storySessionsService).should().getSessions(null, null, null, 1);
        then(worldsService).should().getAllWorlds();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void abandon() throws Exception {
        mockMvc.perform(post(ABANDON, 1L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LIST))
                .andDo(print());

        then(storySessionsService).should().modifyStorySessionByStatus(eq(1L), eq(SessionStatus.ABANDONED));
    }
}
