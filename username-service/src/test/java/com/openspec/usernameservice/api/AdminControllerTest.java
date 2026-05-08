package com.openspec.usernameservice.api;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.openspec.usernameservice.reservation.AdminReservationService;
import com.openspec.usernameservice.reservation.HandleReservation;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminReservationService adminReservationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(adminReservationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listsReservations() throws Exception {
        when(adminReservationService.listRecent(anyInt()))
                .thenReturn(List.of(new HandleReservation("ALA", "alice@example.se", Instant.parse("2026-05-01T10:00:00Z"))));

        mockMvc.perform(get("/admin/handles").param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].handle").value("ALA"))
                .andExpect(jsonPath("$[0].email").value("alice@example.se"));
    }

    @Test
    void deletesOldMappings() throws Exception {
        when(adminReservationService.deleteOlderThanDays(30)).thenReturn(4);

        mockMvc.perform(delete("/admin/handles").param("olderThanDays", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(4))
                .andExpect(jsonPath("$.olderThanDays").value(30));
    }

    @Test
    void returns400WhenOlderThanDaysIsInvalid() throws Exception {
        when(adminReservationService.deleteOlderThanDays(0))
                .thenThrow(new IllegalArgumentException("olderThanDays must be greater than zero"));

        mockMvc.perform(delete("/admin/handles").param("olderThanDays", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid_request"));
    }
}
