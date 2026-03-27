package com.openspec.usernameservice.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openspec.usernameservice.service.HandleAllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HandleControllerTest {

    @Mock
    private HandleAllocationService allocationService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        reset(allocationService);
        mockMvc = MockMvcBuilders.standaloneSetup(new HandleController(allocationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returns201OnSuccess() throws Exception {
        when(allocationService.allocate(anyString())).thenReturn(new HandleResponse("ALA", true));

        mockMvc.perform(post("/usernames")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new HandleRequest("anna@example.se"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.handle").value("ALA"));
    }

    @Test
    void returns400ForInvalidEmail() throws Exception {
        mockMvc.perform(post("/usernames")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new HandleRequest("invalid"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns422WhenAllBlocked() throws Exception {
        when(allocationService.allocate(anyString()))
                .thenThrow(new HandleAllocationService.HandleAllocationException("all_blocked", "Unable", "blocked@example.se"));

        mockMvc.perform(post("/usernames")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new HandleRequest("blocked@example.se"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("all_blocked"));
    }

    @Test
    void returns409WhenCollisions() throws Exception {
        when(allocationService.allocate(anyString()))
                .thenThrow(new HandleAllocationService.HandleAllocationException("collisions_exhausted", "Unable", "taken@example.se"));

        mockMvc.perform(post("/usernames")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new HandleRequest("taken@example.se"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("collisions_exhausted"));
    }

    @Test
    void returns503WhenPersistenceFails() throws Exception {
        when(allocationService.allocate(anyString()))
                .thenThrow(new DataAccessResourceFailureException("DB down"));

        mockMvc.perform(post("/usernames")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new HandleRequest("fail@example.se"))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("persistence_error"));
    }
}
