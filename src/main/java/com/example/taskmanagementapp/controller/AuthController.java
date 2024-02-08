package com.example.taskmanagementapp.controller;

import com.example.taskmanagementapp.dto.AccessTokenResponseDTO;
import com.example.taskmanagementapp.dto.LoginRequestDTO;
import com.example.taskmanagementapp.dto.RegisterRequestDTO;
import com.example.taskmanagementapp.model.RefreshToken;
import com.example.taskmanagementapp.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        String result = authService.register(registerRequestDTO);
        return ResponseEntity.ok("User was registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponseDTO> login(@RequestBody LoginRequestDTO request) {
       AccessTokenResponseDTO loginResponse = authService.login(request);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AccessTokenResponseDTO> refreshToken(@RequestBody RefreshToken refreshToken) {
        AccessTokenResponseDTO accessTokenResponseDTO = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(accessTokenResponseDTO);
    }

}
