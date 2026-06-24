package com.worldtaletalk.notice.controller;

import com.worldtaletalk.notice.dto.NoticesDTO;
import com.worldtaletalk.notice.service.NoticesService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoticeViewController.class)
class NoticeViewControllerTest {

    private static final String LIST = "/notices";
    private static final String DETAIL = "/notices/{id}";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private NoticesService noticesService;
    @MockitoBean private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void list() throws Exception {
        Page<NoticesDTO> dto = new PageImpl<>(List.of(NoticesDTO.builder().id(1L).build()));
        given(noticesService.getActiveNotices(1)).willReturn(dto);

        mockMvc.perform(get(LIST))
                .andExpect(status().isOk())
                .andExpect(view().name("notices/list"))
                .andExpect(model().attribute("notices", dto))
                .andDo(print());

        then(noticesService).should().getActiveNotices(eq(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void detail() throws Exception {
        NoticesDTO dto = NoticesDTO.builder().id(1L).build();
        given(noticesService.getNotice(1L)).willReturn(dto);

        mockMvc.perform(get(DETAIL, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("notices/detail"))
                .andExpect(model().attribute("notice", dto))
                .andDo(print());
        then(noticesService).should().getNotice(eq(1L));
    }
}
