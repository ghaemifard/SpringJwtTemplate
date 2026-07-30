package com.ghaemi.boot.springjwttest.repository;

import com.ghaemi.boot.springjwttest.entity.RefreshToken;
import com.ghaemi.boot.springjwttest.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByToken(String token);

    @Transactional
    @Modifying
    void deleteByUser(User user);

    @Transactional
    @Modifying
    void deleteByToken(String token);



}
