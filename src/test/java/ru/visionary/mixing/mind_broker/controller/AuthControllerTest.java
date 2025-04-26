package ru.visionary.mixing.mind_broker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import ru.visionary.mixing.generated.model.AuthResponse;
import ru.visionary.mixing.generated.model.LoginRequest;
import ru.visionary.mixing.generated.model.RefreshRequest;
import ru.visionary.mixing.generated.model.RegisterRequest;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.security.JwtTokenProvider;
import ru.visionary.mixing.mind_broker.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean({JwtTokenProvider.class, UserDetailsService.class})
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AuthService authService;

    @Test
    void register_ValidRequest_Returns200() throws Exception {
        RegisterRequest request = new RegisterRequest()
                .nickname("testuser")
                .email("test@example.com")
                .password("password");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void login_ValidCredentials_ReturnsTokens() throws Exception {
        LoginRequest request = new LoginRequest()
                .email("valid@example.com")
                .password("password");

        AuthResponse response = new AuthResponse()
                .accessToken("access")
                .refreshToken("refresh");

        doReturn(response).when(authService).login(any());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"));
    }

    @Test
    void refresh_InvalidToken_Returns404() throws Exception {
        RefreshRequest request = new RefreshRequest().refreshToken("invalid");

        doThrow(new ServiceException(ErrorCode.INVALID_REFRESH_TOKEN)).when(authService).refresh(any());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(-5));
    }

    @Test
    void register_ExistingEmail_ReturnsConflict() throws Exception {
        RegisterRequest request = new RegisterRequest()
                .nickname("existing")
                .email("exists@example.com")
                .password("password");

        doThrow(new ServiceException(ErrorCode.EMAIL_ALREADY_IN_USE)).when(authService).register(any());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}