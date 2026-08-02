package com.ghaemi.boot.springjwttest;


import com.ghaemi.boot.springjwttest.entity.Role;
import com.ghaemi.boot.springjwttest.entity.User;
import com.ghaemi.boot.springjwttest.repository.UserRepository;
import com.ghaemi.boot.springjwttest.security.MyRedisUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MyRedisUserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void init() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        testUser = User.builder()
                .id(1L)
                .username("sasan")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .build();
    }


    // 1. verify on cache miss the user loaded from postgres and then the user is written to redis
    @Test
    void loadUserByUsername_shouldReturnUserOnCacheMiss() throws Exception {
        // cache miss
        when(valueOperations.get("user:sasan")).thenReturn(null);
        when(userRepository.findByUsername("sasan")).thenReturn(Optional.of(testUser));
        when(objectMapper.writeValueAsString(testUser)).thenReturn("{\"username\":\"sasan\"}");

        var result = userDetailsService.loadUserByUsername("sasan");

        assertThat(result.getUsername()).isEqualTo("sasan");
        verify(userRepository).findByUsername("sasan");
        verify(valueOperations).set(eq("user:sasan"), anyString(), any(Duration.class));
    }

    // 2. user must be loaded from redis instead of postgres
    @Test
    void loadUserByUsername_shouldReturnCachedUserOnCacheHit() throws Exception {
        String cachedJson = "{\"username\":\"sasan\"}";
        when(valueOperations.get("user:sasan")).thenReturn(cachedJson);
        when(objectMapper.readValue(cachedJson, User.class)).thenReturn(testUser);

        var result = userDetailsService.loadUserByUsername("sasan");

        assertThat(result.getUsername()).isEqualTo("sasan");

        // repository must NOT be called on cache hit
        verify(userRepository, never()).findByUsername(anyString());
    }


    // 3. check if the code throws UsernameNotFoundException: when the user is not in postgres
    @Test
    void loadUserByUsername_shouldThrowWhenUserNotFound() {
        when(valueOperations.get("user:unknown")).thenReturn(null);
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("not found");
    }

    // check what will happen when redis entries are corrupted.
    @Test
    void loadUserByUsername_shouldFallbackToDatabaseWhenCacheIsCorrupted() throws Exception {
        when(valueOperations.get("user:sasan")).thenReturn("corrupted-json");
        when(objectMapper.readValue(anyString(), eq(User.class)))
                .thenThrow(new RuntimeException("JSON parse error"));
        when(userRepository.findByUsername("sasan")).thenReturn(Optional.of(testUser));
        when(objectMapper.writeValueAsString(testUser)).thenReturn("{\"username\":\"sasan\"}");

        var result = userDetailsService.loadUserByUsername("sasan");

        assertThat(result.getUsername()).isEqualTo("sasan");
        verify(userRepository).findByUsername("sasan");
    }
}