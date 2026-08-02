package com.ghaemi.boot.springjwttest;


import com.ghaemi.boot.springjwttest.entity.User;
import com.ghaemi.boot.springjwttest.repository.UserRepository;
import com.ghaemi.boot.springjwttest.security.RefreshTokenRedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private RefreshTokenRedisService refreshTokenService;

    private static final long REFRESH_EXPIRATION_MS = 604_800_000L; // 7 days

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshDurationMs", REFRESH_EXPIRATION_MS);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // 1. check if the ttl and the token are valid
    @Test
    void createRefreshToken_shouldGenerateTokenAndStoreInRedisWithCorrectTtl() {
        String username = "sasan";

        String token = refreshTokenService.createRefreshToken(username);

        assertThat(token).isNotBlank();

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);

        // capture the key and the ttl
        verify(valueOperations).set(
                keyCaptor.capture(),
                eq(username),
                ttlCaptor.capture()
        );

        assertThat(keyCaptor.getValue()).isEqualTo("r_token:" + token);
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofMillis(REFRESH_EXPIRATION_MS));
    }

    // 2. check if the token can be mapped to a username
    @Test
    void findUsernameByToken_shouldReturnUsernameWhenTokenExists() {
        String token = "abccc-123";
        when(valueOperations.get("r_token:" + token)).thenReturn("sasan");

        String username = refreshTokenService.findUsernameByToken(token);

        assertThat(username).isEqualTo("sasan");
    }

    // 3. check a missing token returns null for the username
    @Test
    void findUsernameByToken_shouldReturnNullWhenTokenMissingOrExpired() {
        String token = "missing-token";
        when(valueOperations.get("r_token:" + token)).thenReturn(null);

        String username = refreshTokenService.findUsernameByToken(token);

        assertThat(username).isNull();
    }


    // 4. when token doest not exist in redis, the verify method should fail by throwing a runtime exception
    @Test
    void verifyToken_forExpiredOrMissingToken() {
        when(valueOperations.get(anyString())).thenReturn(null);

        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("invalid-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid");
    }

    // 5.
    @Test
    void deleteByToken_shouldRemoveTokenFromRedis() {
        String token = "token-to-delete";

        refreshTokenService.deleteByToken(token);

        verify(redisTemplate).delete("r_token:" + token);
    }

    // 6. verify the token rotation behavior
    @Test
    void tokenRotation_shouldDeleteOldTokenAndCreateNewOne() {
        String oldToken = "old-token";
        String username = "sasan";

        // simulate that old token exists
        lenient().when(valueOperations.get("r_token:" + oldToken)).thenReturn(username);

        // delete old token (part of the rotation)
        //rotation started here
        refreshTokenService.deleteByToken(oldToken);

        // create new token
        String newToken = refreshTokenService.createRefreshToken(username); // rotaion ended here


        assertThat(newToken).isNotBlank();
        assertThat(newToken).isNotEqualTo(oldToken);

        verify(redisTemplate).delete("r_token:" + oldToken);

        verify(valueOperations).set(
                eq("r_token:" + newToken),
                eq(username),
                any(Duration.class)
        );
    }

    // 7. testing if findByUsername works properly
    @Test
    void getUserFromRefreshToken() {
        String token = "valid-token";
        User user = User.builder().username("sasan").build();

        when(valueOperations.get("r_token:" + token)).thenReturn("sasan");
        when(userRepository.findByUsername("sasan")).thenReturn(Optional.of(user));

        User result = refreshTokenService.findUserByToken(token);

        assertThat(result.getUsername()).isEqualTo("sasan");
    }

}