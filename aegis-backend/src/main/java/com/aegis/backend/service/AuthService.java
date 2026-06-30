package com.aegis.backend.service;

import com.aegis.backend.dto.AuthResponse;
import com.aegis.backend.entity.RefreshToken;
import com.aegis.backend.entity.User;
import com.aegis.backend.mapper.UserMapper;
import com.aegis.backend.repository.RefreshTokenRepository;
import com.aegis.backend.util.JwtUtil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final long refreshExpirationMs;

    public AuthService(
            final RefreshTokenRepository refreshTokenRepository,
            final JwtUtil jwtUtil,
            final UserMapper userMapper,
            @Value("${aegis.security.jwt.refresh-expiration-ms:604800000}") final long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public String createRefreshToken(final User user) {
        // Generate secure raw token
        final String rawToken = UUID.randomUUID().toString();
        final String hashedToken = hashToken(rawToken);

        final RefreshToken refreshToken = RefreshToken.builder()
                .token(hashedToken)
                .user(user)
                .expiryDate(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000L))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Persisted secure hashed refresh token for user '{}'", user.getUsername());
        return rawToken;
    }

    @Transactional
    public AuthResponse rotateRefreshToken(final String rawToken) {
        final String hashedToken = hashToken(rawToken);
        final RefreshToken token = refreshTokenRepository
                .findByToken(hashedToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is invalid or does not exist"));

        if (token.isRevoked()) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token has expired");
        }

        final User user = token.getUser();

        // Rotate: delete the old token and issue a new one
        refreshTokenRepository.delete(token);

        final String newAccessToken = jwtUtil.generateToken(user);
        final String newRawRefreshToken = createRefreshToken(user);

        log.info("Successfully rotated refresh token for user '{}'", user.getUsername());
        return userMapper.toAuthResponse(user, newAccessToken, newRawRefreshToken);
    }

    @Transactional
    public void revokeRefreshToken(final String rawToken) {
        final String hashedToken = hashToken(rawToken);
        final Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(hashedToken);
        if (tokenOpt.isPresent()) {
            final RefreshToken token = tokenOpt.get();
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info(
                    "Successfully revoked refresh token for user '{}'",
                    token.getUser().getUsername());
        } else {
            log.warn("Attempted to revoke non-existent refresh token");
        }
    }

    private String hashToken(final String rawToken) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            final StringBuilder hexString = new StringBuilder();
            for (final byte b : hash) {
                final String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (final Exception exception) {
            log.error("Crypto error during token hashing", exception);
            throw new IllegalStateException("Failed to hash refresh token: " + exception.getMessage(), exception);
        }
    }
}
