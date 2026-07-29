package com.worldtaletalk.world.controller;

import com.worldtaletalk.notice.dto.NoticesDTO;
import com.worldtaletalk.notice.service.NoticesService;
import com.worldtaletalk.user.service.UserService;
import com.worldtaletalk.world.dto.WorldsDTO;
import com.worldtaletalk.world.service.WorldsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    private static final String INDEX = "/";
    private static final String WORLDS = "/worlds";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private WorldsService worldsService;
    @MockitoBean private NoticesService noticesService;
    @MockitoBean private UserService userService;

    @Test
    @WithMockUser
    void home() throws Exception {
        List<WorldsDTO> worlds = List.of(WorldsDTO.builder().id(1L).build());
        List<WorldsDTO> featured = List.of(WorldsDTO.builder().id(2L).build());
        given(worldsService.getActiveWorlds()).willReturn(worlds);
        given(noticesService.getLatestActiveNotices(5)).willReturn(List.of(NoticesDTO.builder().id(1L).build()));

        mockMvc.perform(get(INDEX))
                .andExpect(status().isOk())
                .andExpect(view().name("home/index"))
                .andExpect(model().attribute("worlds", worlds))
                .andExpect(model().attribute("featuredWorlds", featured))
                .andExpect(model().attributeExists("notices"))
                .andDo(print());

        then(worldsService).should().getActiveWorlds();
        then(worldsService).should().getFeaturedWorlds(3);
        then(noticesService).should().getLatestActiveNotices(5);
    }

    @Test
    @WithMockUser
    void worlds() throws Exception {
        List<WorldsDTO> dto = List.of(WorldsDTO.builder().id(1L).build());
        given(worldsService.getActiveWorlds()).willReturn(dto);

        mockMvc.perform(get(WORLDS))
                .andExpect(status().isOk())
                .andExpect(view().name("world/list"))
                .andExpect(model().attribute("worlds", dto))
                .andDo(print());

        then(worldsService).should().getActiveWorlds();
    }
}
