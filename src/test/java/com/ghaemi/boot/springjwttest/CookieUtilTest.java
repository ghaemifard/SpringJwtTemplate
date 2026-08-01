package com.ghaemi.boot.springjwttest;


import com.ghaemi.boot.springjwttest.utils.CookieUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class CookieUtilTest {

    private CookieUtils cookieUtil;
    private MockHttpServletResponse response;
    private static final long REFRESH_EXPIRATION_MS = 604_800_000L; // 7 days

    @BeforeEach
    void setUp() {
        cookieUtil = new CookieUtils();
        ReflectionTestUtils.setField(cookieUtil, "refreshTokenDuration", REFRESH_EXPIRATION_MS);
        response = new MockHttpServletResponse();
    }

    @Test
    void addRefreshTokenCookie() {
        String refreshToken = "test-refresh-token-123";

        cookieUtil.addDeleteRefTokenCookie(response, refreshToken);

        // get the Set-Cookie header and it should not be null
        String setCookieHeader = response.getHeader("Set-Cookie");
        assertThat(setCookieHeader).isNotNull();

        // check cookie name and value
        assertThat(setCookieHeader).contains("ref_token=test-refresh-token-123");

        //HttpOnly
        assertThat(setCookieHeader).containsIgnoringCase("HttpOnly");

        // path
        assertThat(setCookieHeader).contains("Path=/");

        // max-Age (7 days in seconds = 604800)
        assertThat(setCookieHeader).contains("Max-Age=604800");

        // SameSite
        assertThat(setCookieHeader).contains("SameSite=Strict");
    }

    @Test
    void deleteRefreshTokenCookie() {
        cookieUtil.addDeleteRefTokenCookie(response,null);
        // set cookie not null
        String setCookieHeader = response.getHeader("Set-Cookie");
        assertThat(setCookieHeader).isNotNull();

        // cookie name should still be present
        assertThat(setCookieHeader).contains("ref_token=");

        // max-age must be 0 so that the browser would delete the cookie
        assertThat(setCookieHeader).contains("Max-Age=0");

        // still HttpOnly and Path for consistency
        assertThat(setCookieHeader).containsIgnoringCase("HttpOnly");
        assertThat(setCookieHeader).contains("Path=/");
    }
}