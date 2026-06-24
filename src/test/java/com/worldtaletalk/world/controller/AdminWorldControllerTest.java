package com.worldtaletalk.world.controller;

import com.worldtaletalk.user.service.UserService;
import com.worldtaletalk.world.domain.WorldGenre;
import com.worldtaletalk.world.dto.WorldsDTO;
import com.worldtaletalk.world.service.WorldsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminWorldController.class)
class AdminWorldControllerTest {

    private static final String LIST = "/admin/worlds";
    private static final String NEW = "/admin/worlds/new";
    private static final String EDIT_FORM = "/admin/worlds/{id}/edit";
    private static final String UPDATE = "/admin/worlds/{id}";
    private static final String TOGGLE = "/admin/worlds/{id}/toggle";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private WorldsService worldsService;
    @MockitoBean private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void list() throws Exception {
        List<WorldsDTO> dto = List.of(WorldsDTO.builder().id(1L).build());
        given(worldsService.getAllWorlds()).willReturn(dto);

        mockMvc.perform(get(LIST))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/worlds/list"))
                .andExpect(model().attribute("worlds", dto))
                .andDo(print());

        then(worldsService).should().getAllWorlds();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void newWorld() throws Exception {
        mockMvc.perform(get(NEW))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/worlds/form"))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editForm() throws Exception {
        given(worldsService.getWorldById(1L)).willReturn(WorldsDTO.builder().id(1L).build());

        mockMvc.perform(get(EDIT_FORM, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/worlds/form"))
                .andExpect(model().attributeExists("world"))
                .andDo(print());

        then(worldsService).should().getWorldById(eq(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create() throws Exception {
        mockMvc.perform(post(LIST)
                        .param("name", "판타지 세계관")
                        .param("genre", WorldGenre.FANTASY.name())
                        .param("description", "설명")
                        .param("settingText", "LLM 프롬프트")
                        .param("targetTurn", "10")
                        .param("isActive", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LIST))
                .andDo(print());

        then(worldsService).should().addWorld(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_invalid() throws Exception {
        mockMvc.perform(post(LIST)
                        .param("name", "")
                        .param("genre", WorldGenre.FANTASY.name())
                        .param("description", "설명")
                        .param("settingText", "LLM 프롬프트")
                        .param("targetTurn", "10")
                        .param("isActive", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(NEW))
                .andDo(print());

        then(worldsService).should(never()).addWorld(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update() throws Exception {
        mockMvc.perform(post(UPDATE, 1L)
                        .param("name", "수정된 세계관")
                        .param("genre", WorldGenre.SF.name())
                        .param("description", "수정 설명")
                        .param("settingText", "수정 프롬프트")
                        .param("targetTurn", "15")
                        .param("isActive", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LIST))
                .andDo(print());

        then(worldsService).should().modifyWorld(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_invalid() throws Exception {
        mockMvc.perform(post(UPDATE, 1L)
                        .param("name", "")
                        .param("genre", WorldGenre.SF.name())
                        .param("description", "수정 설명")
                        .param("settingText", "수정 프롬프트")
                        .param("targetTurn", "15")
                        .param("isActive", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/worlds/1/edit"))
                .andDo(print());

        then(worldsService).should(never()).modifyWorld(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggle() throws Exception {
        given(worldsService.getWorldById(1L)).willReturn(WorldsDTO.builder().id(1L).active(true).build());

        mockMvc.perform(post(TOGGLE, 1L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LIST))
                .andDo(print());

        then(worldsService).should().getWorldById(eq(1L));
        then(worldsService).should().modifyWorld(any());
    }
}
