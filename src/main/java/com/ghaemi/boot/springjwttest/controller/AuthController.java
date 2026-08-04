package com.ghaemi.boot.springjwttest.controller;


import com.ghaemi.boot.springjwttest.dto.AuthResponse;
import com.ghaemi.boot.springjwttest.dto.LoginRequest;
import com.ghaemi.boot.springjwttest.entity.RefreshToken;
import com.ghaemi.boot.springjwttest.entity.User;
import com.ghaemi.boot.springjwttest.security.JwtService;
import com.ghaemi.boot.springjwttest.security.RefreshTokenRedisService;
import com.ghaemi.boot.springjwttest.security.RefreshTokenService;
import com.ghaemi.boot.springjwttest.utils.CookieUtils;
import com.ghaemi.boot.springjwttest.utils.ServletUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final ServletUtils servletUtils;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRedisService refreshTokenRedisService;

    private final CookieUtils cookieUtils;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,HttpServletRequest hReq, HttpServletResponse response) {

        final String ip = servletUtils.getClientIP(hReq);
        log.info("Login attempt for username:{} - ip:{}",request.username(),ip);
        try{

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(),request.password())

            );

            UserDetails user = userDetailsService.loadUserByUsername(request.username());
            String token = jwtService.generateToken(user);
            //From Postgresql:
//        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
//        cookieUtils.addDeleteRefTokenCookie(response, refreshToken.getToken());

            String refreshToken = refreshTokenRedisService.createRefreshToken(user.getUsername());
            cookieUtils.addDeleteRefTokenCookie(response, refreshToken);

            log.info("Login success for username:{} - ip:{}",request.username(),ip);

            return ResponseEntity.ok(new AuthResponse(token));
        }catch (Exception e){
            log.warn("Login failed for username:{} - ip:{} - reasom:{} ",request.username(),ip,e.getClass().getSimpleName());
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request,HttpServletRequest hReq, HttpServletResponse response) {

        final String ip = servletUtils.getClientIP(hReq);
        log.info("Refresh token request - ip:{}",ip);

        String refTokenStr = cookieUtils.extractRefreshToken(request);
        if(refTokenStr==null){
            log.warn("Refresh token failed. ip={}",ip);
            throw new RuntimeException("Please login again. Invalid cookie");
        }
        // From postgresql
//        RefreshToken refreshToken = refreshTokenService.findByToken(refTokenStr);
//        if(refreshToken==null){
//            throw new RuntimeException("Please login again. Invalid token.");
//        }
//
//        refreshToken = refreshTokenService.verifyRefreshToken(refreshToken);
//
//        UserDetails userDetails = refreshToken.getUser();
//        String newJwtToken = jwtService.generateToken(userDetails);
//
//        refreshTokenService.deleteByToken(refreshToken.getToken());
//        RefreshToken newRefToken   =  refreshTokenService.createRefreshToken(userDetails.getUsername());
//        cookieUtils.addDeleteRefTokenCookie(response, newRefToken.getToken());


        User user = refreshTokenRedisService.findUserByToken(refTokenStr);
        String newJwtToken = jwtService.generateToken(user);

        refreshTokenRedisService.deleteByToken(refTokenStr);
        String newRefreshToken = refreshTokenRedisService.createRefreshToken(user.getUsername());

        cookieUtils.addDeleteRefTokenCookie(response, newRefreshToken);


        log.info("The operation refresh token was successful. username:{} - ip:{}",user.getUsername(),ip);
        return ResponseEntity.ok(new AuthResponse(newJwtToken));

    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request,HttpServletRequest hReq, HttpServletResponse response) {
        String token = cookieUtils.extractRefreshToken(request);
        final String ip = servletUtils.getClientIP(hReq);
        if(token!=null){
            //From postgresql
//            refreshTokenService.deleteByToken(token);
            refreshTokenRedisService.deleteByToken(token);
            log.info("Logout successfful.  ip:{}",ip);
        }else{
            log.debug("Cannot extract refresh token in logout method. ip:{}",ip);
        }

        cookieUtils.addDeleteRefTokenCookie(response, null);

        return ResponseEntity.ok("Successfully logged out.");
    }

}