package com.worldtaletalk.notice.controller;

import com.worldtaletalk.global.security.CustomUserDetails;
import com.worldtaletalk.notice.dto.NoticesDTO;
import com.worldtaletalk.notice.service.NoticesService;
import com.worldtaletalk.user.domain.user.UserRole;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AdminNoticeController.class)
class AdminNoticeControllerTest {

    private static final String LIST = "/admin/notices";
    private static final String NEW = "/admin/notices/new";
    private static final String UPDATE = "/admin/notices/{id}/edit";
    private static final String DELETE = "/admin/notices/{id}";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private NoticesService noticesService;
    @MockitoBean private UserService userService;
    private CustomUserDetails adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new CustomUserDetails(
                UserDTO.builder()
                        .id(1L)
                        .email("admin@test.com")
                        .role(UserRole.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .build()
        );
        given(userService.getUser(any())).willReturn(
                UserDTO.builder()
                        .id(1L)
                        .role(UserRole.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .build()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void list() throws Exception {
        Page<NoticesDTO> dto = new PageImpl<>(List.of(NoticesDTO.builder().id(1L).build()));
        given(noticesService.getAllNotices(1)).willReturn(dto);

        mockMvc.perform(get(LIST))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/notices/list"))
                .andExpect(model().attributeExists("notices"))
                .andExpect(model().attribute("noticesPage", dto))
                .andDo(print());

        then(noticesService).should().getAllNotices(eq(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void noticeForm() throws Exception {
        mockMvc.perform(get(NEW))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/notices/form"))
                .andExpect(model().attributeExists("notice"))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editForm() throws Exception {
        given(noticesService.getNotice(1L)).willReturn(NoticesDTO.builder().id(1L).build());

        mockMvc.perform(get(UPDATE, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/notices/form"))
                .andExpect(model().attributeExists("notice"))
                .andDo(print());

        then(noticesService).should().getNotice(eq(1L));
    }

    @Test
    void create() throws Exception {
        mockMvc.perform(post(LIST)
                        .param("title", "title")
                        .param("content", "content")
                        .param("isActive", "true")
                        .with(csrf())
                        .with(user(adminUser))
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LIST))
                .andDo(print());

        then(noticesService).should().createNotice(any());
    }

    @Test
    void create_invalid() throws Exception {
        mockMvc.perform(post(LIST)
                        .param("title", "")
                        .param("content", "content")
                        .param("isActive", "true")
                        .with(csrf())
                        .with(user(adminUser))
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(NEW))
                .andDo(print());

        then(noticesService).should(never()).createNotice(any());
    }

    @Test
    void update() throws Exception {
        mockMvc.perform(post(UPDATE, 1L)
                        .param("title", "title")
                        .param("content", "content")
                        .param("isActive", "true")
                        .with(csrf())
                        .with(user(adminUser))
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LIST))
                .andDo(print());

        then(noticesService).should().modifyNotice(any());
    }

    @Test
    void update_invalid() throws Exception {
        mockMvc.perform(post(UPDATE, 1L)
                        .param("title", "")
                        .param("content", "content")
                        .param("isActive", "true")
                        .with(csrf())
                        .with(user(adminUser))
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/notices/1/edit"))
                .andDo(print());

        then(noticesService).should(never()).modifyNotice(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete() throws Exception {
        mockMvc.perform(post(DELETE, 1L)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LIST))
                .andDo(print());
        then(noticesService).should().deleteNotice(eq(1L));
    }
}