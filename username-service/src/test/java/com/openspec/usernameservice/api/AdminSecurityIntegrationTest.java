package com.openspec.usernameservice.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

import com.openspec.usernameservice.reservation.AdminReservationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AdminSecurityIntegrationTest {

    @MockitoBean
    private AdminReservationService adminReservationService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void rejectsAnonymousAccessToAdminList() throws Exception {
        mockMvc.perform(get("/admin/handles")).andExpect(status().isUnauthorized());
    }

    @Test
    void allowsAdminAccessToAdminList() throws Exception {
        when(adminReservationService.listRecent(100)).thenReturn(List.of());
        mockMvc.perform(get("/admin/handles").with(httpBasic("admin", "admin"))).andExpect(status().isOk());
    }

    @Test
    void rejectsAnonymousDelete() throws Exception {
        mockMvc.perform(delete("/admin/handles").param("olderThanDays", "30"))
                .andExpect(status().isUnauthorized());
    }
}
