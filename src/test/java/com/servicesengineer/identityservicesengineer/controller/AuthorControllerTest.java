package com.servicesengineer.identityservicesengineer.controller;

import com.servicesengineer.identityservicesengineer.dto.response.AuthorResponse;
import com.servicesengineer.identityservicesengineer.service.AuthorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthorController.class)
@AutoConfigureMockMvc(addFilters = false) // Tắt filter JWT
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorService authorService;

    // Thêm dòng mock JwtTokenProvider nếu cần cho bean khác
    @MockBean
    private com.servicesengineer.identityservicesengineer.configuration.JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("GET /author/{authorId} should return author details")
    void testGetOneAuthor() throws Exception {
        String authorId = "123";
        AuthorResponse authorResponse = AuthorResponse.builder()
                .id(authorId)
                .name("John Doe")
                .build();

        when(authorService.getOneAuthor(authorId)).thenReturn(authorResponse);

        mockMvc.perform(get("/author/{authorId}", authorId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Get One Author Successful")))
                .andExpect(jsonPath("$.result.id", is(authorId)))
                .andExpect(jsonPath("$.result.name", is("John Doe")));
    }
}

