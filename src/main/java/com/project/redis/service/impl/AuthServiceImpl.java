package com.project.redis.service.impl;

import com.project.redis.dto.*;
import com.project.redis.enums.UserRole;
import com.project.redis.enums.UserStatus;
import com.project.redis.exception.InvalidOtpException;
import com.project.redis.exception.OtpExpiredException;
import com.project.redis.exception.UserAlreadyExistsException;
import com.project.redis.model.User;
import com.project.redis.repository.UserRepository;
import com.project.redis.security.JwtService;
import com.project.redis.service.IEmailService;
import com.project.redis.service.IOtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor

public class AuthServiceImpl {
    private final UserRepository userRepository;
    private final IOtpService otpService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final IEmailService emailService;
    private final JwtService jwtService;



    @Transactional
    public ApiResponse<UserResponse> register(RegisterRequest registerRequest) throws UserAlreadyExistsException {
        String email = registerRequest.email().toLowerCase().trim();
        String username = registerRequest.username().toLowerCase().trim();
        if (userRepository.existsUserByEmail(email)) {
            throw new UserAlreadyExistsException("This email already registered!");

        }
        if (userRepository.existsUserByUsername(username)) {
            throw new UserAlreadyExistsException("This username already registered!");

        }
        User user = User.builder()
                .avatar("default.png")
                .createdAt(LocalDateTime.now())

                .email(email)
                .username(username)
                .level(1)
                .totalScore(0L)
                .totalMatches(0)
                .wins(0)
                .losses(0)
                .winRate(0.0)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        try {
            otpService.generateAndSendOtp(email, username);
        } catch (RuntimeException e) {
            log.error("OTP can not be sent: {}", email, e);
        }
        UserResponse userResponse = mapToUserResponse(user);


        return ApiResponse.success(
                "Registration successful! Check your email and log in with the OTP code.",
                userResponse
        );
    }

    public ApiResponse<String> sendOtp(SendOTPRequest otpRequest) {
        User user = userRepository.findUserByEmail(otpRequest.email()).orElseThrow();
        String email = otpRequest.email().toLowerCase().trim();

        if (user.getStatus() == UserStatus.BANNED) {
            return ApiResponse.error("Your account has been blocked. Please contact support.");
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            return ApiResponse.error("Your account has been temporarily suspended.");
        }
        otpService.generateAndSendOtp(email, user.getUsername());
        Long ttl = otpService.getOtpTTL(otpRequest.email());

        return ApiResponse.success(
                "An OTP code has been sent to your email. " + ttl + " valid for seconds.",
                null
        );

    }

    public ApiResponse<AuthResponse> verifyOtpAndLogin(VerifyOTPRequest verifyOTPRequest) throws InvalidOtpException, OtpExpiredException {
        String email = verifyOTPRequest.email().toLowerCase().trim();
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new RuntimeException("This user not found!"));

        otpService.verifyOtp(email, verifyOTPRequest.otp());

        if (user.getEmailVerifiedAt() == null) {
            user.verifyEmail();
            try {
                emailService.sendWelcomeEmail(email, user.getUsername());
            } catch (Exception e) {
                log.error("Welcome email not sent: {}", email, e);
            }

        }
        userRepository.save(user);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        String token = jwtService.generateToken(claims, user);

        String tokenKey = "token:user:" + user.getId();
        redisTemplate.opsForValue().set(
                tokenKey,
                token,
                86400000,
                TimeUnit.MILLISECONDS
        );

        // 7. Online users-ə əlavə et
        redisTemplate.opsForSet().add("online:users", "user:" + user.getId());

        log.info("✅ Login uğurlu: {} (ID: {})", user.getUsername(), user.getId());

        // 8. Response
        UserResponse userResponse = mapToUserResponse(user);

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(userResponse)
                .build();

        return ApiResponse.success("Login success", authResponse);


    }

    public ApiResponse<String> logout(Long id) {
        String key = "token:user:" + id;
        redisTemplate.delete(key);
        redisTemplate.opsForSet().remove("online:users:", "user:" + id);
        return ApiResponse.success("Successful Logout");
    }


    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .level(user.getLevel())
                .totalScore(user.getTotalScore())
                .totalMatches(user.getTotalMatches())
                .wins(user.getWins())
                .losses(user.getLosses())
                .winRate(user.getWinRate())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .emailVerified(user.getEmailVerifiedAt() != null)
                .build();
    }

    public boolean isTokenActive(Long userId, String token) {
        String tokenKey = "token:user:" + userId;
        String storedToken = (String) redisTemplate.opsForValue().get(tokenKey);
        return token.equals(storedToken);
    }
}
