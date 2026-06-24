package com.worldtaletalk.statistics.controller;

import com.worldtaletalk.statistics.dto.AdminDashboardStatsDTO;
import com.worldtaletalk.statistics.service.AdminStatsService;
import com.worldtaletalk.user.service.UserService;
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

@WebMvcTest(AdminDashboardController.class)
class AdminDashboardControllerTest {

    private static final String DASHBOARD = "/admin";
    private static final String STATS = "/admin/stats";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AdminStatsService adminStatsService;
    @MockitoBean private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void dashboard() throws Exception {
        AdminDashboardStatsDTO dto = AdminDashboardStatsDTO.builder()
                .userCount(10).worldCount(5).activeSessionCount(3).pendingReportCount(1).build();
        given(adminStatsService.getDashboardStats()).willReturn(dto);

        mockMvc.perform(get(DASHBOARD))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("stats", dto))
                .andDo(print());

        then(adminStatsService).should().getDashboardStats();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void stats() throws Exception {
        given(adminStatsService.getWorldStats()).willReturn(List.of());

        mockMvc.perform(get(STATS))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/stats/index"))
                .andExpect(model().attribute("worldStats", List.of()))
                .andDo(print());

        then(adminStatsService).should().getWorldStats();
    }
}
