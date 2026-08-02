package com.ghaemi.boot.springjwttest.security;

import com.ghaemi.boot.springjwttest.entity.RefreshToken;
import com.ghaemi.boot.springjwttest.entity.User;
import com.ghaemi.boot.springjwttest.repository.RefreshTokenRepository;
import com.ghaemi.boot.springjwttest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenRedisService {


    private static final String REFRESH_TOKEN_PREFIX = "r_token:";

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshDurationMs;

    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;
    //generate tokes with UUID, then create key, and then save it on redis (PREFIX+UUID,username)
    public String createRefreshToken(String username) {
            String token = UUID.randomUUID().toString();
            String key = REFRESH_TOKEN_PREFIX+token;
            stringRedisTemplate.opsForValue().set(key,username, Duration.ofMillis(refreshDurationMs));


            return token;
    }
    public String findUsernameByToken(String token) {
        return stringRedisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX+token);
    }

    // delete the entry from redis
    public void deleteByToken(String token) {
        stringRedisTemplate.delete(REFRESH_TOKEN_PREFIX+token);
    }

    // get the username (value) from the token(key) on redis, and retrieve the User from repo
    public User findUserByToken(String token) {
        String username = findUsernameByToken(token);
        if(username==null){
            throw new RuntimeException("Invalid refresh token");
        }
        return userRepository.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found"));
    }

    // see if the usernamen (value) is available on redis based on the given token
    public void verifyRefreshToken(String token) {
        String username = findUsernameByToken(token);
        if(username==null){
            throw new RuntimeException("Invalid refresh token");
        }
    }




}
