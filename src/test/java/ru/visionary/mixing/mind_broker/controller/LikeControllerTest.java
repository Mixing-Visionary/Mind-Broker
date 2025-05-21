package ru.visionary.mixing.mind_broker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.service.LikeService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = LikeController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class LikeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private LikeService likeService;

    @Test
    void like_ValidRequest_ReturnsOk() throws Exception {
        UUID imageUuid = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/image/{uuid}/like", imageUuid))
                .andExpect(status().isOk());
    }

    @Test
    void like_AlreadyLiked_ReturnsConflict() throws Exception {
        UUID imageUuid = UUID.randomUUID();
        doThrow(new ServiceException(ErrorCode.ALREADY_LIKED))
                .when(likeService).likeImage(any());

        mockMvc.perform(post("/api/v1/image/{uuid}/like", imageUuid))
                .andExpect(status().isConflict());
    }

    @Test
    void dislike_ValidRequest_ReturnsOk() throws Exception {
        UUID imageUuid = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/image/{uuid}/like", imageUuid))
                .andExpect(status().isOk());
    }

    @Test
    void dislike_NotLiked_ReturnsConflict() throws Exception {
        UUID imageUuid = UUID.randomUUID();
        doThrow(new ServiceException(ErrorCode.NOT_LIKED))
                .when(likeService).dislikeImage(any());

        mockMvc.perform(delete("/api/v1/image/{uuid}/like", imageUuid))
                .andExpect(status().isConflict());
    }
}
