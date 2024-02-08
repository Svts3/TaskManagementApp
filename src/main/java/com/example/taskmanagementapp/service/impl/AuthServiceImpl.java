package com.example.taskmanagementapp.service.impl;

import com.example.taskmanagementapp.dto.AccessTokenResponseDTO;
import com.example.taskmanagementapp.dto.LoginRequestDTO;
import com.example.taskmanagementapp.dto.RegisterRequestDTO;
import com.example.taskmanagementapp.exception.RefreshTokenExpiredException;
import com.example.taskmanagementapp.exception.RoleNotFoundException;
import com.example.taskmanagementapp.exception.UserExistException;
import com.example.taskmanagementapp.model.RefreshToken;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.repository.RoleRepository;
import com.example.taskmanagementapp.security.CustomUserDetailsService;
import com.example.taskmanagementapp.security.JwtTokenProvider;
import com.example.taskmanagementapp.security.RefreshTokenProvider;
import com.example.taskmanagementapp.service.AuthService;
import com.example.taskmanagementapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private PasswordEncoder passwordEncoder;

    private CustomUserDetailsService customUserDetailsService;

    private UserService userService;

    private JwtTokenProvider jwtTokenProvider;

    private RoleRepository roleRepository;

    private AuthenticationManager authenticationManager;

    private RefreshTokenProvider refreshTokenProvider;

    @Autowired
    public AuthServiceImpl(PasswordEncoder passwordEncoder,
                           CustomUserDetailsService customUserDetailsService,
                           UserService userService, JwtTokenProvider jwtTokenProvider,
                           RoleRepository roleRepository,
                           AuthenticationManager authenticationManager,
                           RefreshTokenProvider refreshTokenProvider) {
        this.passwordEncoder = passwordEncoder;
        this.customUserDetailsService = customUserDetailsService;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.refreshTokenProvider = refreshTokenProvider;
    }

    @Override
    public String register(RegisterRequestDTO registerRequestDTO) {
        if (userService.existsByEmail(registerRequestDTO.getEmail())) {
            throw new UserExistException(String.format("User %s email already exists", registerRequestDTO.getEmail()));
        }
        User user = User
                .builder()
                .firstName(registerRequestDTO.getFirstName())
                .lastName(registerRequestDTO.getLastName())
                .email(registerRequestDTO.getEmail())
                .password(passwordEncoder.encode(registerRequestDTO.getPassword()))
                .roles(List.of(roleRepository.findByName("ROLE_USER")
                        .orElseThrow(()->new RoleNotFoundException("Role ROLE_USER was not found!"))))
                .build();
        userService.save(user);
        return "User was registered successfully!";
    }

    @Override
    public AccessTokenResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );
        User user = (User) customUserDetailsService.loadUserByUsername(request.getEmail());
        String accessToken = jwtTokenProvider.generateToken(user);
        RefreshToken refreshToken = refreshTokenProvider.generateRefreshToken(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        AccessTokenResponseDTO loginResponse = AccessTokenResponseDTO
                .builder().accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
        return loginResponse;
    }

    @Override
    public AccessTokenResponseDTO refreshToken(RefreshToken token) {
        RefreshToken refreshToken1 = refreshTokenProvider.findByToken(token.getToken());
        if (!refreshTokenProvider.validateRefreshToken(refreshToken1.getToken())) {
            throw new RefreshTokenExpiredException(String.format("%s refresh token is expired!", refreshToken1.getToken()));

        }
        String accessToken = jwtTokenProvider.generateToken(refreshToken1.getUser());
        AccessTokenResponseDTO accessTokenResponseDTO = AccessTokenResponseDTO
                .builder()
                .refreshToken(refreshToken1.getToken())
                .accessToken(accessToken)
                .build();
        return accessTokenResponseDTO;
    }
}
