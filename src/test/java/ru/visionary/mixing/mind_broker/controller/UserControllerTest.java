package ru.visionary.mixing.mind_broker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.visionary.mixing.generated.model.UserResponse;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.service.FollowService;
import ru.visionary.mixing.mind_broker.service.ImageService;
import ru.visionary.mixing.mind_broker.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ImageService imageService;
    @MockBean
    private UserService userService;
    @MockBean
    private FollowService followService;

    @Test
    void getUser_ValidRequest_ReturnsUser() throws Exception {
        UserResponse response = new UserResponse()
                .userId(1L)
                .nickname("testuser")
                .email("test@example.com");

        when(userService.getUser(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/user/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.nickname").value("testuser"));
    }

    @Test
    void getUser_UserNotFound_Returns404() throws Exception {
        when(userService.getUser(999L)).thenThrow(new ServiceException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get("/api/v1/user/{userId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(-7));
    }

    @Test
    void getCurrentUser_ValidRequest_ReturnsUser() throws Exception {
        UserResponse response = new UserResponse()
                .userId(1L)
                .nickname("currentUser")
                .email("current@example.com");

        when(userService.getCurrentUser()).thenReturn(response);

        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.nickname").value("currentUser"));
    }

    @Test
    void getCurrentUser_Unauthenticated_Returns401() throws Exception {
        when(userService.getCurrentUser())
                .thenThrow(new ServiceException(ErrorCode.USER_NOT_AUTHORIZED));

        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value(-11));
    }

    @Test
    void getCurrentUser_UserDeleted_Returns410() throws Exception {
        when(userService.getCurrentUser())
                .thenThrow(new ServiceException(ErrorCode.CURRENT_USER_DELETED));

        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.errorCode").value(-14));
    }

    @Test
    void updateUser_ValidRequest_Returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "test.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/user/{userId}", 1L)
                        .file(file)
                        .param("nickname", "newnick")
                        .param("description", "new desc")
                        .param("password", "mynewpass")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_AdminUpdatesOtherUser_Returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "avatar", "test.jpg", "image/jpeg", "content".getBytes());

        mockMvc.perform(multipart("/api/v1/user/{userId}", 2L)
                        .file(file)
                        .param("nickname", "newnick")
                        .param("description", "new desc")
                        .param("password", "mynewpass")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_UnauthorizedAccess_Returns403() throws Exception {
        doThrow(new ServiceException(ErrorCode.ACCESS_FORBIDDEN))
                .when(userService).updateUser(anyLong(), any(), any(), any(), any());

        mockMvc.perform(multipart("/api/v1/user/{userId}", 2L)
                        .param("nickname", "newnick")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCurrentUser_ValidRequest_Returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/user")
                        .file(file)
                        .param("nickname", "newNick")
                        .param("description", "new desc")
                        .param("password", "newPass123")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    @Test
    void updateCurrentUser_Unauthenticated_Returns401() throws Exception {
        doThrow(new ServiceException(ErrorCode.USER_NOT_AUTHORIZED))
                .when(userService).updateCurrentUser(any(), any(), any(), any());

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/user")
                        .param("nickname", "test")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateCurrentUser_InvalidFileFormat_Returns415() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "avatar",
                "test.png",
                "image/png",
                "content".getBytes()
        );

        doThrow(new ServiceException(ErrorCode.FILE_FORMAT_NOT_SUPPORTED))
                .when(userService).updateCurrentUser(any(), any(), any(), any());

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/user")
                        .file(invalidFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void deleteUser_AdminAccess_Returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/user/{userId}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_UnauthorizedAccess_Returns403() throws Exception {
        doThrow(new ServiceException(ErrorCode.ACCESS_FORBIDDEN))
                .when(userService).deleteUser(anyLong());

        mockMvc.perform(delete("/api/v1/user/{userId}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCurrentUser_ValidRequest_Returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/user"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCurrentUser_Unauthenticated_Returns401() throws Exception {
        doThrow(new ServiceException(ErrorCode.USER_NOT_AUTHORIZED))
                .when(userService).deleteCurrentUser();

        mockMvc.perform(delete("/api/v1/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteAvatar_Unauthorized_Returns401() throws Exception {
        doThrow(new ServiceException(ErrorCode.USER_NOT_AUTHORIZED))
                .when(userService).deleteAvatar(anyLong());

        mockMvc.perform(delete("/api/v1/user/{userId}/avatar", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteAvatar_ValidRequest_Returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/user/{userId}/avatar", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void deleteAvatar_UserNotFound_Returns404() throws Exception {
        doThrow(new ServiceException(ErrorCode.USER_NOT_FOUND))
                .when(userService).deleteAvatar(anyLong());

        mockMvc.perform(delete("/api/v1/user/{userId}/avatar", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCurrentAvatar_ValidRequest_Returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/user/avatar"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCurrentAvatar_Unauthenticated_Returns401() throws Exception {
        doThrow(new ServiceException(ErrorCode.USER_NOT_AUTHORIZED))
                .when(userService).deleteCurrentAvatar();

        mockMvc.perform(delete("/api/v1/user/avatar"))
                .andExpect(status().isUnauthorized());
    }
}