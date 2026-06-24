package com.worldtaletalk.user.controller;

import com.worldtaletalk.global.security.CustomUserDetails;
import com.worldtaletalk.global.security.oauth2.CustomOAuth2User;
import com.worldtaletalk.user.domain.user.UserRole;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.dto.UserSuspensionsDTO;
import com.worldtaletalk.user.service.UserService;
import com.worldtaletalk.user.service.UserSuspensionsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    private static final String LOGIN = "/auth/login";
    private static final String NICKNAME = "/auth/nickname";
    private static final String SUSPENDED = "/auth/suspended";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;
    @MockitoBean private UserSuspensionsService userSuspensionsService;
    private CustomUserDetails loginUser;
    private OAuth2AuthenticationToken oauthToken;

    @BeforeEach
    void setUp() {
        UserDTO userDTO = UserDTO.builder().id(1L).email("user@test.com").role(UserRole.USER).status(UserStatus.ACTIVE).build();
        loginUser = new CustomUserDetails(userDTO);
        CustomOAuth2User oauthUser = new CustomOAuth2User(userDTO, Map.of(), false);
        oauthToken = new OAuth2AuthenticationToken(oauthUser, oauthUser.getAuthorities(), "google");

        given(userService.getUser(any())).willReturn(
                UserDTO.builder().id(1L).role(UserRole.USER).status(UserStatus.ACTIVE).build()
        );
    }

    @Test
    @WithMockUser
    void login() throws Exception {
        mockMvc.perform(get(LOGIN))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andDo(print());
    }

    @Test
    @WithMockUser
    void nicknameForm() throws Exception {
        mockMvc.perform(get(NICKNAME))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/nickname"))
                .andDo(print());
    }

    @Test
    void submitNickname() throws Exception {
        given(userService.existsByNickname("newNick")).willReturn(false);

        mockMvc.perform(post(NICKNAME)
                        .param("nickname", "newNick")
                        .with(csrf())
                        .with(authentication(oauthToken)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andDo(print());

        then(userService).should().modifyNickname(eq(1L), eq("newNick"));
    }

    @Test
    void submitNickname_invalid() throws Exception {
        mockMvc.perform(post(NICKNAME)
                        .param("nickname", "")
                        .with(csrf())
                        .with(authentication(oauthToken)))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/nickname"))
                .andExpect(model().attributeExists("nicknameError"))
                .andDo(print());
    }

    @Test
    void submitNickname_duplicate() throws Exception {
        given(userService.existsByNickname("dupNick")).willReturn(true);

        mockMvc.perform(post(NICKNAME)
                        .param("nickname", "dupNick")
                        .with(csrf())
                        .with(authentication(oauthToken)))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/nickname"))
                .andExpect(model().attributeExists("nicknameError"))
                .andDo(print());

        then(userService).should(never()).modifyNickname(any(), any());
    }

    @Test
    void suspended() throws Exception {
        given(userSuspensionsService.getUserSuspension(1L)).willReturn(UserSuspensionsDTO.builder().id(1L).build());

        mockMvc.perform(get(SUSPENDED).with(user(loginUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/suspended"))
                .andExpect(model().attributeExists("suspended"))
                .andDo(print());

        then(userSuspensionsService).should().getUserSuspension(eq(1L));
    }
}
