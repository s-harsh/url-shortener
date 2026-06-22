package com.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.dto.request.ShortenUrlRequest;
import com.urlshortener.dto.response.ShortenUrlResponse;
import com.urlshortener.exception.GlobalExceptionHandler;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
@Import(GlobalExceptionHandler.class)
class UrlControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private UrlService urlService;

    @Test
    void POST_shorten_valid_request_returns_201() throws Exception {
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setUrl("https://www.example.com/very/long/path");

        ShortenUrlResponse response = ShortenUrlResponse.builder()
                .shortCode("abc1234")
                .shortUrl("http://localhost:8080/abc1234")
                .originalUrl("https://www.example.com/very/long/path")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(365))
                .build();

        when(urlService.shortenUrl(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("abc1234"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/abc1234"));
    }

    @Test
    void POST_shorten_blank_url_returns_400() throws Exception {
        ShortenUrlRequest request = new ShortenUrlRequest();
        request.setUrl("");

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GET_info_not_found_returns_404() throws Exception {
        when(urlService.getUrlInfo("notfound")).thenThrow(new UrlNotFoundException("notfound"));

        mockMvc.perform(get("/api/v1/urls/notfound"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void DELETE_url_returns_204() throws Exception {
        mockMvc.perform(delete("/api/v1/urls/abc1234"))
                .andExpect(status().isNoContent());
    }
}
