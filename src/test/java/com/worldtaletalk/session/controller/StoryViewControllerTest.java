package com.worldtaletalk.session.controller;

import com.worldtaletalk.chat.service.ChatMessagesService;
import com.worldtaletalk.inventory.service.SessionItemsService;
import com.worldtaletalk.persona.dto.PersonasDTO;
import com.worldtaletalk.persona.service.PersonasService;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.session.service.StorySessionsService;
import com.worldtaletalk.stat.dto.SessionStatSnapshotsDTO;
import com.worldtaletalk.stat.service.SessionStatsService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoryViewController.class)
class StoryViewControllerTest {

    private static final String PLAY = "/sessions/{id}/play";
    private static final String ABANDON = "/sessions/{id}/abandon";
    private static final String ENDING = "/sessions/{id}/ending";
    private static final String EPILOGUE = "/sessions/{id}/epilogue";
    private static final String COMPLETE = "/sessions/{id}/complete";
    private static final String DROP_ITEM = "/sessions/{sessionId}/items/{itemId}/drop";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private StorySessionsService storySessionsService;
    @MockitoBean private WorldsService worldsService;
    @MockitoBean private PersonasService personasService;
    @MockitoBean private ChatMessagesService chatMessagesService;
    @MockitoBean private SessionItemsService sessionItemsService;
    @MockitoBean private SessionStatsService sessionStatsService;
    @MockitoBean private UserService userService;

    @Test
    @WithMockUser
    void play() throws Exception {
        StorySessionsDTO session = StorySessionsDTO.builder().id(1L).worldId(1L).personaId(1L).build();
        given(storySessionsService.getStorySession(1L)).willReturn(session);
        given(sessionStatsService.getCurrentStats(1L)).willReturn(Map.of());
        given(worldsService.getWorldById(1L)).willReturn(WorldsDTO.builder().id(1L).build());
        given(personasService.getPersona(1L)).willReturn(PersonasDTO.builder().id(1L).build());
        given(chatMessagesService.getChatMessages(eq(1L), isNull())).willReturn(List.of());
        given(sessionItemsService.getInventory(1L)).willReturn(List.of());

        mockMvc.perform(get(PLAY, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("story/play"))
                .andExpect(model().attributeExists("storySession"))
                .andExpect(model().attributeExists("world"))
                .andExpect(model().attributeExists("persona"))
                .andExpect(model().attributeExists("messages"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("currentStats"))
                .andDo(print());

        then(storySessionsService).should().getStorySession(eq(1L));
    }

    @Test
    @WithMockUser
    void abandon() throws Exception {
        mockMvc.perform(post(ABANDON, 1L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage/sessions"))
                .andDo(print());

        then(storySessionsService).should().modifyStorySessionByStatus(eq(1L), eq(SessionStatus.ABANDONED));
    }

    @Test
    @WithMockUser
    void ending() throws Exception {
        StorySessionsDTO session = StorySessionsDTO.builder().id(1L).worldId(1L).personaId(1L).build();
        given(storySessionsService.getStorySession(1L)).willReturn(session);
        given(worldsService.getWorldById(1L)).willReturn(WorldsDTO.builder().id(1L).build());
        given(personasService.getPersona(1L)).willReturn(PersonasDTO.builder().id(1L).build());

        mockMvc.perform(get(ENDING, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("story/ending"))
                .andExpect(model().attributeExists("storySession"))
                .andExpect(model().attributeExists("world"))
                .andExpect(model().attributeExists("persona"))
                .andDo(print());
    }

    @Test
    @WithMockUser
    void epilogue_post() throws Exception {
        mockMvc.perform(post(EPILOGUE, 1L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions/1/epilogue"))
                .andDo(print());

        then(storySessionsService).should().modifyStorySessionByStatus(eq(1L), eq(SessionStatus.EPILOGUE));
    }

    @Test
    @WithMockUser
    void epilogue_get() throws Exception {
        StorySessionsDTO session = StorySessionsDTO.builder().id(1L).worldId(1L).personaId(1L).build();
        given(storySessionsService.getStorySession(1L)).willReturn(session);
        given(sessionStatsService.getSnapshot(1L)).willReturn(SessionStatSnapshotsDTO.builder().id(1L).build());
        given(chatMessagesService.getChatMessages(eq(1L), isNull())).willReturn(List.of());

        mockMvc.perform(get(EPILOGUE, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("story/epilogue"))
                .andExpect(model().attributeExists("storySession"))
                .andExpect(model().attributeExists("snapshotStats"))
                .andExpect(model().attributeExists("messages"))
                .andDo(print());
    }

    @Test
    @WithMockUser
    void complete() throws Exception {
        StorySessionsDTO session = StorySessionsDTO.builder().id(1L).worldId(1L).build();
        given(storySessionsService.getStorySession(1L)).willReturn(session);

        mockMvc.perform(post(COMPLETE, 1L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/worlds/1/ratings?sessionId=1"))
                .andDo(print());

        then(storySessionsService).should().modifyStorySessionByStatus(eq(1L), eq(SessionStatus.COMPLETED));
    }

    @Test
    @WithMockUser
    void drop() throws Exception {
        mockMvc.perform(post(DROP_ITEM, 1L, 2L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions/1/play"))
                .andDo(print());

        then(sessionItemsService).should().dropItem(eq(1L), eq(2L));
    }
}
