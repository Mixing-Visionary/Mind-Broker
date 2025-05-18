package ru.visionary.mixing.mind_broker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.visionary.mixing.generated.model.GetCommentsResponse;
import ru.visionary.mixing.generated.model.SaveCommentRequest;
import ru.visionary.mixing.mind_broker.service.CommentService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = CommentController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Test
    void saveComment_ValidRequest_ReturnsOk() throws Exception {
        UUID imageUuid = UUID.randomUUID();
        SaveCommentRequest request = new SaveCommentRequest().comment("Test comment");

        doNothing().when(commentService).saveComment(any(), any());

        mockMvc.perform(post("/api/v1/image/{uuid}/comment", imageUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"Test comment\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void getComments_ValidRequest_ReturnsOk() throws Exception {
        UUID imageUuid = UUID.randomUUID();
        GetCommentsResponse response = new GetCommentsResponse();

        when(commentService.getComments(any(), anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/v1/image/{uuid}/comments", imageUuid)
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isOk());
    }
}