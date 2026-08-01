package com.ghaemi.boot.springjwttest;


import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncoderTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void shouldEncodePasswordAndMatchSuccessfully() {
        String rawPassword = "password";

        String encodedPassword = passwordEncoder.encode(rawPassword);

        // not qual to the raw pass
        assertThat(encodedPassword).isNotEqualTo(rawPassword);

        // should start with the BCrypt prefix
        assertThat(encodedPassword).startsWith("$2a$");

        // matching must succeed
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    // this test is not enough
    @Test
    void shouldNotMatchWrongPassword() {
        String rawPassword = "password";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertThat(passwordEncoder.matches("wrong-password", encodedPassword)).isFalse();
    }
}