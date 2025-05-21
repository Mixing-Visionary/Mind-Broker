package ru.visionary.mixing.mind_broker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.visionary.mixing.generated.model.UsersResponse;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.service.FollowService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = FollowController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class FollowControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private FollowService followService;

    @Test
    void follow_ValidRequest_Returns200() throws Exception {
        mockMvc.perform(post("/api/v1/user/{userId}/follow", 2L)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void follow_UserNotFound_Returns404() throws Exception {
        doThrow(new ServiceException(ErrorCode.USER_NOT_FOUND))
                .when(followService).follow(anyLong());

        mockMvc.perform(post("/api/v1/user/{userId}/follow", 999L)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unfollow_ValidRequest_Returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/user/{userId}/follow", 2L)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void unfollow_NotFollowing_Returns409() throws Exception {
        doThrow(new ServiceException(ErrorCode.NOT_FOLLOWING))
                .when(followService).unfollow(anyLong());

        mockMvc.perform(delete("/api/v1/user/{userId}/follow", 2L)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isConflict());
    }

    @Test
    void getCurrentFollowers_ValidRequest_Returns200() throws Exception {
        UsersResponse response = new UsersResponse().users(Collections.emptyList());
        when(followService.getCurrentFollowers(anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/v1/user/followers")
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").exists());
    }

    @Test
    void getCurrentFollowers_Unauthorized_Returns401() throws Exception {
        when(followService.getCurrentFollowers(anyInt(), anyInt()))
                .thenThrow(new ServiceException(ErrorCode.USER_NOT_AUTHORIZED));

        mockMvc.perform(get("/api/v1/user/followers")
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentFollowers_UserDeleted_Returns410() throws Exception {
        when(followService.getCurrentFollowers(anyInt(), anyInt()))
                .thenThrow(new ServiceException(ErrorCode.CURRENT_USER_DELETED));

        mockMvc.perform(get("/api/v1/user/followers")
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isGone());
    }

    @Test
    void getCurrentFollows_ValidRequest_Returns200() throws Exception {
        UsersResponse response = new UsersResponse().users(Collections.emptyList());
        when(followService.getCurrentFollows(anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/v1/user/follows")
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").exists());
    }

    @Test
    void getCurrentFollows_MissingParameters_Returns400() throws Exception {
        mockMvc.perform(get("/api/v1/user/follows"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFollowers_ValidRequest_Returns200() throws Exception {
        UsersResponse response = new UsersResponse().users(Collections.emptyList());
        when(followService.getFollowers(anyLong(), anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/v1/user/{userId}/followers", 1L)
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").exists());
    }

    @Test
    void getFollowers_UserNotFound_Returns404() throws Exception {
        when(followService.getFollowers(anyLong(), anyInt(), anyInt()))
                .thenThrow(new ServiceException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get("/api/v1/user/{userId}/followers", 999L)
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFollowers_UserDeleted_Returns410() throws Exception {
        when(followService.getFollowers(anyLong(), anyInt(), anyInt()))
                .thenThrow(new ServiceException(ErrorCode.OWNER_DELETED));

        mockMvc.perform(get("/api/v1/user/{userId}/followers", 1L)
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isGone());
    }

    @Test
    void getFollows_ValidRequest_Returns200() throws Exception {
        UsersResponse response = new UsersResponse().users(Collections.emptyList());
        when(followService.getFollows(anyLong(), anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/v1/user/{userId}/follows", 1L)
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").exists());
    }

    @Test
    void getFollows_InvalidPagination_Returns400() throws Exception {
        mockMvc.perform(get("/api/v1/user/{userId}/follows", 1L)
                        .param("size", "-1")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }
}
