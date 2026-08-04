package com.ghaemi.boot.springjwttest.config;

import com.ghaemi.boot.springjwttest.entity.Role;
import com.ghaemi.boot.springjwttest.entity.User;
import com.ghaemi.boot.springjwttest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) throws Exception {
        if(userRepository.count() == 0) {
            User user = User.builder().username("user001")
                    .password(passwordEncoder.encode("passworD00!"))
                    .enabled(true)
                    .role(Role.ROLE_USER).build();

            User adUser = User.builder().username("admin001").password(passwordEncoder.encode("adminadmiN001!"))
                    .role(Role.ROLE_ADMIN).enabled(true).build();
            userRepository.saveAll(List.of(user,adUser));
            log.info("Test users created: user001/passworD00! and admin001/adminadmiN001");
        }
    }
}
