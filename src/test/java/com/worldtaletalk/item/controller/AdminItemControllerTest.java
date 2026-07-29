package com.worldtaletalk.item.controller;

import com.worldtaletalk.item.dto.ItemGradeConfigsDTO;
import com.worldtaletalk.item.service.ItemGradeConfigsService;
import com.worldtaletalk.user.service.UserService;
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


@WebMvcTest(AdminItemController.class)
class AdminItemControllerTest {

    private static final String GRADES = "/admin/items/grades";
    private static final String GRADES_ID = "/admin/items/grades/{id}";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ItemGradeConfigsService itemGradeConfigsService;
    @MockitoBean private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void grades() throws Exception {
        List<ItemGradeConfigsDTO> dto = List.of(ItemGradeConfigsDTO.builder().id(1L).build());
        given(itemGradeConfigsService.getGradeConfigs()).willReturn(dto);

        mockMvc.perform(get(GRADES))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/items/grades"))
                .andExpect(model().attribute("gradeConfigs", dto))
                .andDo(print());
        then(itemGradeConfigsService).should().getGradeConfigs();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGrade() throws Exception {
        mockMvc.perform(post(GRADES_ID, 1L)
                .param("statSumMax", "100")
                .param("singleMax", "50")
                .param("singleMin", "0")
                .param("canNegative", "false")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/items/grades"))
                .andDo(print());

        then(itemGradeConfigsService).should().modifyGradeConfig(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGrade_invalid() throws Exception {
        mockMvc.perform(post(GRADES_ID, 1L)
                        .param("statSumMax", "")
                        .param("singleMax", "50")
                        .param("singleMin", "0")
                        .param("canNegative", "false")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/items/grades"))
                .andDo(print());

        then(itemGradeConfigsService).should(never()).modifyGradeConfig(any(), any());
    }

}
