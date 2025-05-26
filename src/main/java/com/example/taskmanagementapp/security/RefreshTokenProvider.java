package com.example.taskmanagementapp.security;

import com.example.taskmanagementapp.exception.RefreshTokenNotFoundException;
import com.example.taskmanagementapp.model.RefreshToken;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class RefreshTokenProvider {


    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public RefreshTokenProvider(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Value("${refresh.token.expiration}")
    private Long expiration;

    @Transactional
    public RefreshToken generateRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken
                .builder()
                .token(UUID.randomUUID().toString())
                .expirationDate(new Date(System.currentTimeMillis() + expiration))
                .user(user)
                .build();
        if(this.existsByUserId(user.getId())){
            this.deleteTokenByUserId(user.getId());
        }
        return refreshTokenRepository.save(refreshToken);
    }

    public Boolean validateRefreshToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        if (new Date().after(refreshToken.getExpirationDate())) {
            refreshTokenRepository.delete(refreshToken);
            return false;
        }
        return true;

    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token).orElseThrow(
                () -> new RefreshTokenNotFoundException(String.format("%s refresh token was not found!", token)));
    }

    public Boolean existsByUserId(Long id){
        return refreshTokenRepository.existsByUserId(id);
    }
    public void deleteTokenByUserId(Long id){
        refreshTokenRepository.deleteByUserId(id);
    }



}
