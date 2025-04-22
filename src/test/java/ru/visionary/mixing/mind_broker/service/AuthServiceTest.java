package ru.visionary.mixing.mind_broker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.visionary.mixing.mind_broker.entity.RefreshToken;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.generated.model.AuthResponse;
import ru.visionary.mixing.generated.model.LoginRequest;
import ru.visionary.mixing.generated.model.RefreshRequest;
import ru.visionary.mixing.generated.model.RegisterRequest;
import ru.visionary.mixing.mind_broker.repository.RefreshTokenRepository;
import ru.visionary.mixing.mind_broker.repository.UserRepository;
import ru.visionary.mixing.mind_broker.security.JwtTokenProvider;
import ru.visionary.mixing.mind_broker.service.mapper.AuthMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_NewUser_SavesUser() {
        RegisterRequest request = new RegisterRequest()
                .nickname("newuser")
                .email("new@example.com")
                .password("password");

        doReturn(null).when(userRepository).findByEmailOrNickname(any(), any());
        doReturn(new User()).when(authMapper).requestToUser(any());
        doAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        }).when(userRepository).save(any());

        assertDoesNotThrow(() -> authService.register(request));
        verify(userRepository).save(any());
    }

    @Test
    void register_ExistingEmail_ThrowsException() {
        RegisterRequest request = new RegisterRequest()
                .nickname("newuser")
                .email("existing@example.com")
                .password("password");

        User existing = new User()
                .setEmail("existing@example.com");

        doReturn(existing).when(userRepository).findByEmailOrNickname(any(), any());

        ServiceException ex = assertThrows(ServiceException.class, () -> authService.register(request));
        assertEquals(ErrorCode.EMAIL_ALREADY_IN_USE, ex.getErrorCode());
    }

    @Test
    void register_ExistingNickname_ThrowsException() {
        RegisterRequest request = new RegisterRequest()
                .nickname("existing")
                .email("new@example.com")
                .password("password");

        User existing = new User()
                .setNickname("existing")
                .setEmail("old@example.com");

        doReturn(existing).when(userRepository).findByEmailOrNickname(any(), any());

        ServiceException ex = assertThrows(ServiceException.class, () -> authService.register(request));
        assertEquals(ErrorCode.NICKNAME_ALREADY_IN_USE, ex.getErrorCode());
    }

    @Test
    void login_ValidCredentials_ReturnsTokens() {
        LoginRequest request = new LoginRequest()
                .email("valid@example.com")
                .password("password");
        User user = new User()
                .setId(1L)
                .setEmail("valid@example.com")
                .setPassword("encoded");
        String accessToken = "access";
        String refreshToken = "refresh";

        doReturn(user).when(userRepository).findByEmail(any());
        doReturn(true).when(passwordEncoder).matches(any(), any());
        doReturn(accessToken).when(jwtTokenProvider).generateAccessToken(any());
        doReturn(refreshToken).when(jwtTokenProvider).generateRefreshToken(any());

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(refreshTokenRepository).deleteByUser(1L);
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        LoginRequest request = new LoginRequest()
                .email("valid@example.com")
                .password("wrong");
        User user = new User()
                .setEmail("valid@example.com")
                .setPassword("encoded");

        doReturn(user).when(userRepository).findByEmail(any());
        doReturn(false).when(passwordEncoder).matches(any(), any());

        ServiceException ex = assertThrows(ServiceException.class, () -> authService.login(request));
        assertEquals(ErrorCode.INVALID_CREDENTIALS, ex.getErrorCode());
    }

    @Test
    void login_ShouldDeleteExistingTokens() {
        LoginRequest request = new LoginRequest()
                .email("user@example.com")
                .password("pass");

        User user = new User();
        user.setId(1L);
        user.setPassword("encoded");

        doReturn(user).when(userRepository).findByEmail(any());
        doReturn(true).when(passwordEncoder).matches(any(), any());

        authService.login(request);

        verify(refreshTokenRepository).deleteByUser(1L);
    }

    @Test
    void refresh_ValidToken_ReturnsNewTokens() {
        RefreshRequest request = new RefreshRequest().refreshToken("valid-refresh");

        User user = new User();
        user.setId(1L);

        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(user);
        storedToken.setExpiryDate(LocalDateTime.now().plusDays(1));

        String accessToken = "new-access";
        String refreshToken = "new-refresh";

        doReturn(true).when(jwtTokenProvider).validateToken(any());
        doReturn(storedToken).when(refreshTokenRepository).findByToken(any());
        doReturn(accessToken).when(jwtTokenProvider).generateAccessToken(any());
        doReturn(refreshToken).when(jwtTokenProvider).generateRefreshToken(any());

        AuthResponse response = authService.refresh(request);

        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(refreshTokenRepository).deleteByUser(1L);
    }

    @Test
    void refresh_InvalidToken_ThrowsException() {
        RefreshRequest request = new RefreshRequest().refreshToken("invalid");

        doReturn(false).when(jwtTokenProvider).validateToken(any());

        ServiceException ex = assertThrows(ServiceException.class, () -> authService.refresh(request));
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, ex.getErrorCode());
    }

    @Test
    void refresh_ExpiredToken_ThrowsException() {
        RefreshRequest request = new RefreshRequest().refreshToken("expired");

        RefreshToken storedToken = new RefreshToken();
        storedToken.setExpiryDate(LocalDateTime.now().minusDays(1));

        doReturn(true).when(jwtTokenProvider).validateToken(any());
        doReturn(storedToken).when(refreshTokenRepository).findByToken(any());

        ServiceException ex = assertThrows(ServiceException.class, () -> authService.refresh(request));
        assertEquals(ErrorCode.EXPIRED_REFRESH_TOKEN, ex.getErrorCode());
    }
}