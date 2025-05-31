package com.example.taskmanagementapp.controller;

import com.example.taskmanagementapp.dto.AccessTokenResponseDTO;
import com.example.taskmanagementapp.dto.LoginRequestDTO;
import com.example.taskmanagementapp.dto.RegisterRequestDTO;
import com.example.taskmanagementapp.model.RefreshToken;
import com.example.taskmanagementapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO) {
        authService.register(registerRequestDTO);
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
