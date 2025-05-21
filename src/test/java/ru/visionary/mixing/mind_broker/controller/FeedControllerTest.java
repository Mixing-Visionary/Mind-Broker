package ru.visionary.mixing.mind_broker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.visionary.mixing.generated.model.GetImagesResponse;
import ru.visionary.mixing.mind_broker.service.FeedService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = FeedController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeedService feedService;

    @Test
    void getFeed_ValidRequest_Returns200() throws Exception {
        when(feedService.getFeed("NEW", 10, 0))
                .thenReturn(new GetImagesResponse());

        mockMvc.perform(get("/api/v1/feed")
                        .param("sort", "NEW")
                        .param("size", "10")
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images").exists());
    }

    @Test
    void getFeed_MissingSort_Returns400() throws Exception {
        mockMvc.perform(get("/api/v1/feed")
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isBadRequest());
    }
}