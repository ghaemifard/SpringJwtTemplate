package com.ghaemi.boot.springjwttest.security;

import com.ghaemi.boot.springjwttest.entity.RefreshToken;
import com.ghaemi.boot.springjwttest.entity.User;
import com.ghaemi.boot.springjwttest.repository.RefreshTokenRepository;
import com.ghaemi.boot.springjwttest.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshDurationMs;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(()->new RuntimeException("User not found"));
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder().expiryDate(Instant.now().plusMillis(refreshDurationMs))
                .token(UUID.randomUUID().toString()).user(user).build();

        return refreshTokenRepository.save(refreshToken);

    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);

    }
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token).orElseThrow(()->new RuntimeException("Invalid refresh token"));
    }
    public RefreshToken verifyRefreshToken(RefreshToken refreshToken) {
        if(refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.deleteByToken(refreshToken.getToken());
            throw new RuntimeException("refreshtoken is expired");
        }
        return refreshToken;
    }


}
