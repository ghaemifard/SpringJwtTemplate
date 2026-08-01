package com.ghaemi.boot.springjwttest;


import com.ghaemi.boot.springjwttest.security.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    @Mock
    private UserDetails userDetails;
    @Mock
    private UserDetails otherUserDetails;
    private static final String SECRET = "thisthisthisthisthisthisthisthisthisthisthis";
    private static final long EXPIRATION_MS = 900_000L; // 15 min

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION_MS);
    }

    /*
       1. Generate token
       Assert username is not blank
       Assrt token has 3 dots
       Assert the subject of the token equals to the username of UserDetails
       check the correctness of issuedAt and expiration
     */
    @Test
    void generateToken() {
        when(userDetails.getUsername()).thenReturn("Mamali");

        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature, so 3 dots

        // Subject
        assertThat(jwtService.extractUsername(token)).isEqualTo("Mamali");

        // Issued-at should be very recent (within the last few seconds)
        Date issuedAt = jwtService.extractClaim(token, claims -> claims.getIssuedAt());
        assertThat(issuedAt).isCloseTo(new Date(), 5000); // 5 seconds tolerance

        // Expiration should be roughly now + (15 minutes or 900_000 Ms)
        Date expiration = jwtService.extractClaim(token, claims -> claims.getExpiration());
        long expectedExpiry = System.currentTimeMillis() + EXPIRATION_MS;
        assertThat(expiration.getTime()).isCloseTo(expectedExpiry, within(5000L));

    }

    // 3. Validate token – success
    @Test
    void isTokenValid() {
        when(userDetails.getUsername()).thenReturn("Saman");

        String token = jwtService.generateToken(userDetails);

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isTrue();
    }


    // 4. Validate token – expired
    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() {

        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);

        when(userDetails.getUsername()).thenReturn("Sasan");
        String token = jwtService.generateToken(userDetails);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean valid = jwtService.isTokenValid(token, userDetails);
        assertThat(valid).isFalse();

//        assertThatThrownBy(() -> jwtService.isTokenValid(token, userDetails))
//                .isInstanceOf(ExpiredJwtException.class);
    }

    // 5. Validate token – wrong user
    @Test
    void isTokenValid_whenUsernameDoesNotMatch() {
        when(userDetails.getUsername()).thenReturn("ali");
        when(otherUserDetails.getUsername()).thenReturn("eli");

        String token = jwtService.generateToken(userDetails);

        boolean valid = jwtService.isTokenValid(token, otherUserDetails);

        assertThat(valid).isFalse();
    }


    // 6. Invalid / malformed token
    @Test
    void extractUsername_shouldThrowForMalformedToken() {
        String malformedToken = "this.is.not.a.valid.jwt";

        assertThatThrownBy(() -> jwtService.extractUsername(malformedToken))
                .isInstanceOfAny(
                        MalformedJwtException.class,
                        IllegalArgumentException.class,
                        SignatureException.class
                );
    }

    //7 invalid signature, but correct header and payload
    @Test
    void extractUsername_shouldThrowForTokenWithInvalidSignature() {
        when(userDetails.getUsername()).thenReturn("sam");
        String token = jwtService.generateToken(userDetails);

        // fake signature
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + ".invalidsignature";

        assertThatThrownBy(() -> jwtService.extractUsername(tampered))
                .isInstanceOf(SignatureException.class);
    }

    // 8. partially invalid token
    @Test
    void isTokenValid_shouldReturnFalseOrThrowForCorruptedToken() {
        String corrupted = "eyJhbGciOiJIUzI1NiJ9.corrupted.payload";


        try {
            boolean valid = jwtService.isTokenValid(corrupted, userDetails);
            assertThat(valid).isFalse();
        } catch (Exception ex) {
            assertThat(ex).isInstanceOfAny(
                    MalformedJwtException.class,
                    SignatureException.class,
                    IllegalArgumentException.class,
                    ExpiredJwtException.class
            );
        }
    }
}