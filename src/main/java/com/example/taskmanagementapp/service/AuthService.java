package com.example.taskmanagementapp.service;

import com.example.taskmanagementapp.dto.LoginRequestDTO;
import com.example.taskmanagementapp.dto.AccessTokenResponseDTO;
import com.example.taskmanagementapp.dto.RegisterRequestDTO;
import com.example.taskmanagementapp.model.RefreshToken;

public interface AuthService {

    String register(RegisterRequestDTO registerRequestDTO);

    AccessTokenResponseDTO login(LoginRequestDTO request);

    AccessTokenResponseDTO refreshToken(RefreshToken token);

}
