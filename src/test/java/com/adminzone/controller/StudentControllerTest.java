package com.adminzone.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnStudentsWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    void shouldAllowProfessorToViewStudents() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PROFESSOR")
    void shouldDenyProfessorToCreateStudent() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType("application/json")
                        .content("{\"nume\":\"Test\",\"prenume\":\"User\",\"email\":\"test@test.com\",\"anStudiu\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToCreateStudent() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType("application/json")
                        .content("{\"nume\":\"Test\",\"prenume\":\"User\",\"email\":\"newstudent@test.com\",\"telefon\":\"0721000000\",\"anStudiu\":1}"))
                .andExpect(status().isCreated());
    }
}
