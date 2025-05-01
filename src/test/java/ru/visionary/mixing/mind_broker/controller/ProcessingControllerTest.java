package ru.visionary.mixing.mind_broker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.visionary.mixing.generated.model.ProcessingImageResponse;
import ru.visionary.mixing.generated.model.StylesResponse;
import ru.visionary.mixing.mind_broker.service.ProcessingService;
import ru.visionary.mixing.mind_broker.service.StyleService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ProcessingController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ProcessingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProcessingService processingService;
    @MockBean
    private StyleService styleService;

    @Test
    void processImage_ValidRequest_ReturnsProcessingId() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "content".getBytes());
        when(processingService.processImage(any(), any(), any()))
                .thenReturn(new ProcessingImageResponse(UUID.randomUUID()));

        mockMvc.perform(multipart("/api/v1/processing")
                        .file(file)
                        .param("style", "test")
                        .param("strength", "0.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").exists());
    }

    @Test
    void getStyles_ShouldReturnStyleList() throws Exception {
        when(styleService.getStyles()).thenReturn(new StylesResponse(List.of()));

        mockMvc.perform(get("/api/v1/styles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.styles").exists());
    }
}