package com.ghaemi.boot.springjwttest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username is rquired")
        @Size(min = 5, max = 50, message = "username must be between 5 and 50 characters")
        String username,
        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[^a-zA-Z0-9]).+$",
                message = "Password must contain at least one number and one special character"
        )
        @Size(min = 8, max = 80, message = "username must be between 8 and 80 characters")
        String password) {
}
