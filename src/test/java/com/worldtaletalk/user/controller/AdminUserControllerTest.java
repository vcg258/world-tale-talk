package com.worldtaletalk.user.controller;

import com.worldtaletalk.global.security.CustomUserDetails;
import com.worldtaletalk.user.domain.user.UserRole;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.dto.UserSuspensionsDTO;
import com.worldtaletalk.user.service.SocialAccountsService;
import com.worldtaletalk.user.service.UserService;
import com.worldtaletalk.user.service.UserSuspensionsService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    private static final String LIST = "/admin/users";
    private static final String DETAIL = "/admin/users/{id}";
    private static final String SUSPEND = "/admin/users/{id}/suspend";
    private static final String LIFT = "/admin/users/{id}/lift";
    private static final String DELETE = "/admin/users/{id}/delete";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    @MockitoBean private UserSuspensionsService userSuspensionsService;
    @MockitoBean private SocialAccountsService socialAccountsService;
    private CustomUserDetails adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new CustomUserDetails(
                UserDTO.builder().id(1L).email("admin@test.com").role(UserRole.ADMIN).status(UserStatus.ACTIVE).build()
        );
        given(userService.getUser(any())).willReturn(
                UserDTO.builder().id(1L).role(UserRole.ADMIN).status(UserStatus.ACTIVE).build()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void list() throws Exception {
        Page<UserDTO> dto = new PageImpl<>(List.of(UserDTO.builder().id(1L).build()));
        given(userService.getUsers(any(), any(), anyInt())).willReturn(dto);

        mockMvc.perform(get(LIST))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/list"))
                .andExpect(model().attribute("users", dto.getContent()))
                .andExpect(model().attribute("usersPage", dto))
                .andDo(print());

        then(userService).should().getUsers(null, null, 1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void detail() throws Exception {
        given(socialAccountsService.getSocialAccountsByUserId(1L)).willReturn(List.of());
        given(userSuspensionsService.getAllUserSuspensions(1L)).willReturn(List.of());

        mockMvc.perform(get(DETAIL, 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/detail"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("socialAccounts"))
                .andExpect(model().attributeExists("suspensions"))
                .andDo(print());

        then(userService).should().getUser(eq(1L));
        then(socialAccountsService).should().getSocialAccountsByUserId(eq(1L));
        then(userSuspensionsService).should().getAllUserSuspensions(eq(1L));
    }

    @Test
    void suspend() throws Exception {
        mockMvc.perform(post(SUSPEND, 1L)
                        .param("reason", "부적절한 행동")
                        .with(csrf())
                        .with(user(adminUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/1"))
                .andDo(print());

        then(userSuspensionsService).should().addUserSuspension(any());
    }

    @Test
    void suspend_invalid() throws Exception {
        mockMvc.perform(post(SUSPEND, 1L)
                        .param("reason", "")
                        .with(csrf())
                        .with(user(adminUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/1"))
                .andDo(print());

        then(userSuspensionsService).should(never()).addUserSuspension(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void lift() throws Exception {
        given(userSuspensionsService.getUserSuspension(1L)).willReturn(UserSuspensionsDTO.builder().id(1L).build());

        mockMvc.perform(post(LIFT, 1L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/1"))
                .andDo(print());

        then(userSuspensionsService).should().getUserSuspension(eq(1L));
        then(userSuspensionsService).should().liftUserSuspension(eq(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete() throws Exception {
        mockMvc.perform(post(DELETE, 1L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(LIST))
                .andDo(print());

        then(userService).should().deleteUser(eq(1L));
    }
}
