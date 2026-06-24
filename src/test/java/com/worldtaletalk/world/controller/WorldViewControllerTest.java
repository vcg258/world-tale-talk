package com.worldtaletalk.world.controller;

import com.worldtaletalk.persona.dto.PersonasDTO;
import com.worldtaletalk.persona.service.PersonasBaseStatsService;
import com.worldtaletalk.persona.service.PersonasService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorldViewController.class)
class WorldViewControllerTest {

    private static final String DETAIL = "/worlds/{id}";
    private static final String PERSONAS = "/worlds/{id}/personas";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private WorldsService worldsService;
    @MockitoBean private PersonasService personasService;
    @MockitoBean private UserService userService;

    @Test
    @WithMockUser
    void detail() throws Exception {
        given(worldsService.getWorldById(1L)).willReturn(WorldsDTO.builder().id(1L).build());

        mockMvc.perform(get(DETAIL, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("world/detail"))
                .andExpect(model().attributeExists("world"))
                .andDo(print());

        then(worldsService).should().getWorldById(eq(1L));
    }

    @Test
    @WithMockUser
    void personas() throws Exception {
        given(worldsService.getWorldById(1L)).willReturn(WorldsDTO.builder().id(1L).build());
        given(personasService.getPersonasByWorldIdWithBaseStats(1L)).willReturn(
                List.of(PersonasDTO.builder().id(1L).baseStats(null).build())
        );

        mockMvc.perform(get(PERSONAS, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("persona/list"))
                .andExpect(model().attributeExists("world"))
                .andExpect(model().attributeExists("personas"))
                .andDo(print());

        then(personasService).should().getPersonasByWorldIdWithBaseStats(eq(1L));
    }
}
