package com.worldtaletalk.report.controller;

import com.worldtaletalk.global.security.CustomUserDetails;
import com.worldtaletalk.report.domain.ReportTargetType;
import com.worldtaletalk.report.service.ReportsService;
import com.worldtaletalk.user.domain.user.UserRole;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportViewController.class)
class ReportViewControllerTest {

    private static final String FORM = "/reports/new";
    private static final String CREATE = "/reports";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ReportsService reportsService;
    @MockitoBean private UserService userService;
    private CustomUserDetails loginUser;

    @BeforeEach
    void setUp() {
        loginUser = new CustomUserDetails(
                UserDTO.builder().id(1L).email("user@test.com").role(UserRole.USER).status(UserStatus.ACTIVE).build()
        );
        given(userService.getUser(any())).willReturn(
                UserDTO.builder().id(1L).role(UserRole.USER).status(UserStatus.ACTIVE).build()
        );
    }

    @Test
    @WithMockUser
    void reportForm() throws Exception {
        mockMvc.perform(get(FORM)
                        .param("targetType", ReportTargetType.REVIEW.name())
                        .param("targetId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("report/form"))
                .andExpect(model().attribute("targetType", ReportTargetType.REVIEW))
                .andExpect(model().attributeExists("targetId"))
                .andDo(print());
    }

    @Test
    void report() throws Exception {
        mockMvc.perform(post(CREATE)
                        .param("targetType", ReportTargetType.REVIEW.name())
                        .param("targetId", "1")
                        .param("reason", "욕설")
                        .with(csrf())
                        .with(user(loginUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andDo(print());

        then(reportsService).should().addReport(any());
    }

    @Test
    void report_invalid() throws Exception {
        mockMvc.perform(post(CREATE)
                        .param("targetType", ReportTargetType.REVIEW.name())
                        .param("targetId", "1")
                        .param("reason", "")
                        .with(csrf())
                        .with(user(loginUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports/new?targetType=REVIEW&targetId=1"))
                .andDo(print());

        then(reportsService).should(never()).addReport(any());
    }
}
