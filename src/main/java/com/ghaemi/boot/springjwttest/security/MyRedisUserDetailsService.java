package com.ghaemi.boot.springjwttest.security;

import com.ghaemi.boot.springjwttest.entity.User;
import com.ghaemi.boot.springjwttest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

//import com.fasterxml.jackson.databind.ObjectMapper;
//jackson-databind
@Service
@RequiredArgsConstructor
public class MyRedisUserDetailsService implements UserDetailsService {

    private static final String USER_CACHE_PREFIX = "user:";
    private static final Duration USER_CACHE_TTL = Duration.ofMinutes(20);

    private final StringRedisTemplate template;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String cKey = USER_CACHE_PREFIX + username;
        String cValue = template.opsForValue().get(cKey);
        if(cValue != null) {
            try{
                return objectMapper.readValue(cValue, User.class);
            }
            catch(Exception e) {
                IO.println("JSON parse error");
            }
        }
        User user = userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("User not found for: "+username));

        template.opsForValue().set(cKey, objectMapper.writeValueAsString(user),USER_CACHE_TTL);

        return user;


    }

    public void evictUserCache(String username) {
            template.delete(USER_CACHE_PREFIX + username);
    }
}
