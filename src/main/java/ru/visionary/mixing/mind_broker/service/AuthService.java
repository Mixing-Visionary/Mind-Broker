package ru.visionary.mixing.mind_broker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.visionary.mixing.generated.model.AuthResponse;
import ru.visionary.mixing.generated.model.LoginRequest;
import ru.visionary.mixing.generated.model.RefreshRequest;
import ru.visionary.mixing.generated.model.RegisterRequest;
import ru.visionary.mixing.mind_broker.entity.RefreshToken;
import ru.visionary.mixing.mind_broker.entity.User;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;
import ru.visionary.mixing.mind_broker.repository.RefreshTokenRepository;
import ru.visionary.mixing.mind_broker.repository.UserRepository;
import ru.visionary.mixing.mind_broker.security.JwtTokenProvider;
import ru.visionary.mixing.mind_broker.service.mapper.AuthMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;

    public void register(RegisterRequest request) {
        log.info("Registering user: email={}", request.getEmail());

        log.debug("Checking existing user for email: {} or nickname: {}", request.getEmail(), request.getNickname());
        User user = userRepository.findByEmailOrNickname(request.getEmail(), request.getNickname());

        if (user != null) {
            if (user.email().equals(request.getEmail())) {
                log.warn("Registration conflict - email already exists: {}", request.getEmail());
                throw new ServiceException(ErrorCode.EMAIL_ALREADY_IN_USE);
            }
            log.warn("Registration conflict - nickname {} already exists", request.getNickname());
            throw new ServiceException(ErrorCode.NICKNAME_ALREADY_IN_USE);
        }

        log.info("Creating new user: {}", request.getEmail());

        userRepository.save(authMapper.requestToUser(request));

        log.debug("User registered successfully: {}", request.getEmail());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        log.debug("Attempting login for email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail());

        if (user == null) {
            log.warn("Login failed - user not found: {}", request.getEmail());
            throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password attempt for user: {}", request.getEmail());
            throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (!user.active()) {
            log.warn("Login failed - user is inactive: {}", request.getEmail());
            throw new ServiceException(ErrorCode.USER_DELETED);
        }

        log.info("Generating tokens for user: {}", user.email());
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        refreshTokenRepository.deleteByUser(user.id());
        refreshTokenRepository.save(new RefreshToken(
                null, refreshToken, user,
                LocalDateTime.now().plusDays(30))
        );

        log.debug("Successful login for: {}", request.getEmail());

        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new ServiceException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken());

        if (storedToken == null) {
            throw new ServiceException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (!storedToken.user().active()) {
            throw new ServiceException(ErrorCode.USER_DELETED);
        }

        if (storedToken.expiryDate().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(storedToken.user());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(storedToken.user());

        refreshTokenRepository.deleteByUser(storedToken.user().id());
        refreshTokenRepository.save(new RefreshToken(null, newRefreshToken, storedToken.user(),
                LocalDateTime.now().plusDays(30)));

        return new AuthResponse(newAccessToken, newRefreshToken);
    }
}
