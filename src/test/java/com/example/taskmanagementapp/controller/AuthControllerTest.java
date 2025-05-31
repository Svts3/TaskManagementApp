package com.example.taskmanagementapp.controller;

import com.example.taskmanagementapp.dto.AccessTokenResponseDTO;
import com.example.taskmanagementapp.dto.LoginRequestDTO;
import com.example.taskmanagementapp.dto.RegisterRequestDTO;
import com.example.taskmanagementapp.exception.handler.GlobalExceptionHandler;
import com.example.taskmanagementapp.model.RefreshToken;
import com.example.taskmanagementapp.model.Role;
import com.example.taskmanagementapp.repository.RoleRepository;
import com.example.taskmanagementapp.security.RefreshTokenProvider;
import com.example.taskmanagementapp.security.config.SecurityConfig;
import com.example.taskmanagementapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import({SecurityConfig.class})
@Transactional
@Testcontainers
// TODO: replace data preparation with service methods instead of controller methods to reduce coupling
public class AuthControllerTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>(
            "mysql:8.0"
    );
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AuthController authController;

    @Autowired
    RefreshTokenProvider refreshTokenProvider;

    @Autowired
    FilterChainProxy filterChainProxy;

    @Autowired
    UserService userService;

    @Autowired
    RoleRepository roleRepository;


    @BeforeEach
    void setUp() {
        mockMvc  = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .apply(springSecurity(filterChainProxy))
                .build();
        roleRepository.save(new Role("ROLE_USER"));
        roleRepository.save(new Role("ROLE_ADMIN"));
    }

    private RegisterRequestDTO createValidRegisterRequest() {
        return RegisterRequestDTO
                .builder()
                .firstName("fName")
                .lastName("lName")
                .email("test.test@gmail.com")
                .password("test")
                .build();
    }

    private LoginRequestDTO createValidLoginRequest() {
        return LoginRequestDTO
                .builder()
                .email("test.test@gmail.com")
                .password("test")
                .build();
    }

    private void registerValidUser() {
        authController.register(createValidRegisterRequest());
    }

    @Test
    void testRegister_WithValidData_ReturnOk() throws Exception {
        RegisterRequestDTO registerRequestDTO = createValidRegisterRequest();

        mockMvc.perform(post("/auth/register")
                        .content(objectMapper.writeValueAsString(registerRequestDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$").exists()
                );
    }

    @Test
    void testRegister_WithNullObject_ReturnBadRequest() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .content("{}").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.message").exists()
                );
    }

    @Test
    void testRegister_WithDuplicateEmail_ReturnBadRequest() throws Exception {
        RegisterRequestDTO registerRequestDTO = createValidRegisterRequest();
        registerValidUser();

        mockMvc.perform(post("/auth/register")
                        .content(objectMapper.writeValueAsString(registerRequestDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.message").exists()
                );
    }

    @Test
    void testLogin_WithValidData_ReturnOk() throws Exception {
        registerValidUser();
        LoginRequestDTO loginRequestDTO = createValidLoginRequest();

        mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(loginRequestDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.accessToken").exists(),
                        jsonPath("$.refreshToken").exists()
                );
    }

    @Test
    void testLogin_WithInvalidPassword_ReturnUnAuthorized() throws Exception {
        registerValidUser();

        LoginRequestDTO loginRequestDTO = LoginRequestDTO
                .builder()
                .email("test.test@gmail.com")
                .password("")
                .build();

        mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(loginRequestDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()),
                        jsonPath("$.message").exists()
                );
    }

    @Test
    void testLogin_WithInvalidEmail_ReturnNotFound() throws Exception {
        registerValidUser();

        LoginRequestDTO loginRequestDTO = LoginRequestDTO
                .builder()
                .email("non-existent@email.com")
                .password("test")
                .build();

        mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(loginRequestDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()),
                        jsonPath("$.message").exists()
                );
    }

    private AccessTokenResponseDTO loginUser() {
        registerValidUser();
        return authController.login(createValidLoginRequest()).getBody();
    }

    @Test
    void testRefreshToken_WithValidToken_ReturnOk() throws Exception {
        AccessTokenResponseDTO accessTokenResponseDTO = loginUser();

        RefreshToken refreshToken = RefreshToken
                .builder()
                .token(refreshTokenProvider.findByToken(accessTokenResponseDTO.getRefreshToken()).getToken())
                .build();
        String content = objectMapper.writeValueAsString(refreshToken);

        mockMvc.perform(post("/auth/refresh-token")
                        .content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.accessToken").exists(),
                        jsonPath("$.refreshToken").exists()
                );
    }

    @Test
    void testRefreshToken_WithInvalidToken_ReturnNotFound() throws Exception {

        RefreshToken refreshToken = RefreshToken
                .builder()
                .token("")
                .build();
        String content = objectMapper.writeValueAsString(refreshToken);
        mockMvc.perform(post("/auth/refresh-token")
                        .content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        content().contentType(MediaType.APPLICATION_JSON),
                        status().isNotFound(),
                        jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()),
                        jsonPath("$.message").exists()
                );
    }

    @Test
    void testRegister_WithInvalidEmail_ReturnBadRequest() throws Exception {
        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO
                .builder()
                .firstName("fName")
                .lastName("lName")
                .email("invalid-email")
                .password("test")
                .build();

        mockMvc.perform(post("/auth/register")
                        .content(objectMapper.writeValueAsString(registerRequestDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.message").exists()
                );
    }

    @Test
    void testRegister_WithEmptyFields_ReturnBadRequest() throws Exception {
        RegisterRequestDTO registerRequestDTO = RegisterRequestDTO
                .builder()
                .firstName("")
                .lastName("")
                .email("test@email.com")
                .password("test")
                .build();

        mockMvc.perform(post("/auth/register")
                        .content(objectMapper.writeValueAsString(registerRequestDTO)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.message").exists()
                );
    }

}
