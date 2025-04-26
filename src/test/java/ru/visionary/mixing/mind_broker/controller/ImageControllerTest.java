package ru.visionary.mixing.mind_broker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.visionary.mixing.generated.model.ImageResponse;
import ru.visionary.mixing.generated.model.SaveImageResponse;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.service.ImageService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ImageController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ImageControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ImageService imageService;

    @Test
    void saveImage_ValidRequest_ReturnsOk() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        UUID uuid = UUID.randomUUID();

        doReturn(new SaveImageResponse().uuid(uuid)).when(imageService).saveImage(any(), anyString());

        mockMvc.perform(multipart("/api/v1/image/save")
                        .file(file)
                        .param("protection", "public")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()));
    }

    @Test
    void saveImage_Unauthorized_Returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        doThrow(new ServiceException(ErrorCode.USER_NOT_AUTHORIZED)).when(imageService).saveImage(any(), anyString());

        mockMvc.perform(multipart("/api/v1/image/save")
                        .file(file)
                        .param("protection", "public")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getImage_UnauthenticatedPublicImage_ReturnsOk() throws Exception {
        UUID uuid = UUID.randomUUID();
        ImageResponse response = new ImageResponse().uuid(uuid).protection(ImageResponse.ProtectionEnum.PUBLIC);

        when(imageService.getImage(uuid)).thenReturn(response);

        mockMvc.perform(get("/api/v1/image/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(uuid.toString()));
    }

    @Test
    void deleteImage_AdminRole_ReturnsOk() throws Exception {
        UUID uuid = UUID.randomUUID();
        doNothing().when(imageService).deleteById(uuid);

        mockMvc.perform(delete("/api/v1/image/{uuid}", uuid)
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }
}