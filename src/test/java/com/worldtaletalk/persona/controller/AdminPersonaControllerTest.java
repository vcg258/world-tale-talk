package com.worldtaletalk.persona.controller;

import com.worldtaletalk.persona.dto.PersonasBaseStatsDTO;
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

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AdminPersonaController.class)
class AdminPersonaControllerTest {

    private static final String LIST = "/admin/personas";
    private static final String NEW = "/admin/personas/new";
    private static final String EDIT_FORM = "/admin/personas/{id}/edit";
    private static final String CREATE = "/admin/personas";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private PersonasService personasService;
    @MockitoBean private PersonasBaseStatsService personasBaseStatsService;
    @MockitoBean private WorldsService worldsService;
    @MockitoBean private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void list() throws Exception {
        List<PersonasDTO> personasDTOS = List.of(PersonasDTO.builder().id(1L).build());
        List<WorldsDTO> worldsDTOS = List.of(WorldsDTO.builder().id(1L).build());
        given(personasService.getPersonasList()).willReturn(personasDTOS);
        given(worldsService.getAllWorlds()).willReturn(worldsDTOS);

        mockMvc.perform(get(LIST))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/personas/list"))
                .andExpect(model().attribute("personas", personasDTOS))
                .andExpect(model().attribute("worlds", worldsDTOS))
                .andExpect(model().attribute("worldId", nullValue()))
                .andDo(print());

        then(personasService).should().getPersonasList();
        then(worldsService).should().getAllWorlds();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void newForm() throws Exception {
        List<WorldsDTO> worldsDTOS = List.of(WorldsDTO.builder().id(1L).build());
        given(worldsService.getAllWorlds()).willReturn(worldsDTOS);

        mockMvc.perform(get(NEW))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/personas/form"))
                .andExpect(model().attribute("worlds", worldsDTOS))
                .andDo(print());

        then(worldsService).should().getAllWorlds();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editForm() throws Exception {
        List<WorldsDTO> worldsDTOS = List.of(WorldsDTO.builder().id(1L).build());
        given(personasService.getPersona(1L)).willReturn(PersonasDTO.builder().id(1L).build());
        given(personasBaseStatsService.getPersonasBaseStatsByPersonaId(1L)).willReturn(PersonasBaseStatsDTO.builder().id(1L).build());
        given(worldsService.getAllWorlds()).willReturn(worldsDTOS);

        mockMvc.perform(get(EDIT_FORM, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/personas/form"))
                .andExpect(model().attributeExists("persona"))
                .andExpect(model().attributeExists("baseStats"))
                .andExpect(model().attribute("worlds", worldsDTOS))
                .andDo(print());

        then(personasService).should().getPersona(1L);
        then(personasBaseStatsService).should().getPersonasBaseStatsByPersonaId(1L);
        then(worldsService).should().getAllWorlds();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create() throws Exception {
        mockMvc.perform(post(CREATE)
                        .param("worldId", "1")
                        .param("name", "test")
                        .param("description", "test")
                        .param("background", "test")
                        .param("isActive", "true")
                        .param("wisdom", "10")
                        .param("strength", "10")
                        .param("agility", "10")
                        .param("charisma", "10")
                        .param("luck", "10")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LIST))
                .andDo(print());

        then(personasService).should().addPersona(any());
        then(personasBaseStatsService).should().addPersonaBaseStats(any());
    }
}
