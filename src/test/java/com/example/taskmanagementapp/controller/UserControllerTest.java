package com.example.taskmanagementapp.controller;


import com.example.taskmanagementapp.dto.mappers.UserMapper;
import com.example.taskmanagementapp.exception.handler.GlobalExceptionHandler;
import com.example.taskmanagementapp.model.Role;
import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import com.example.taskmanagementapp.repository.RoleRepository;
import com.example.taskmanagementapp.security.config.SecurityConfig;
import com.example.taskmanagementapp.service.TaskService;
import com.example.taskmanagementapp.service.UserService;
import com.example.taskmanagementapp.service.WorkspaceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import({SecurityConfig.class})
@Transactional
@Testcontainers
public class UserControllerTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>(
            "mysql"
    );


    MockMvc mockMvc;

    @Autowired
    UserService userService;


    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserController userController;

    @Autowired
    FilterChainProxy filterChainProxy;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WorkspaceService workspaceService;

    @Autowired
    MutableAclService mutableAclService;

    @Autowired
    TaskService taskService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    User user;

    Role roleUser;
    Role roleAdmin;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Test
    void connectionEstablished() {
        Assertions.assertTrue(mySQLContainer.isCreated());
        Assertions.assertTrue(mySQLContainer.isRunning());
    }


    @BeforeEach
    void setUp() {
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss. SSS"));
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .apply(springSecurity(filterChainProxy))
                .build();
        roleUser = roleRepository.save(new Role(1L, "ROLE_USER"));
        roleAdmin = roleRepository.save(new Role(2L, "ROLE_ADMIN"));
        List<Role> roles = new ArrayList<>();
        roles.add(roleUser);
        user = User
                .builder()
                .firstName("fName")
                .lastName("lName")
                .email("test.test@gmail.com")
                .password("pass")
                .roles(roles)
                .build();
        userService.save(user);

    }

    @Test
    void testFindAll_ReturnOk() throws Exception {
        user.setRoles(List.of(roleAdmin));
        mockMvc.perform(get("/users/").with(user(user))
                        .content(List.of(UserMapper.USER_MAPPER.userToUserDTO(user))
                                .toString()))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON)
                );
    }

    @Test
    void testFindById_WithValidId_ReturnOk() throws Exception {
        mockMvc.perform(get("/users/{id}", user.getId()).with(user(user))
                        .content(List.of(UserMapper.USER_MAPPER.userToUserDTO(user))
                                .toString()))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON)
                );
    }

    @Test
    void testFindById_WithIdMismatch_ReturnBadRequest() throws Exception {
        User user2 = User
                .builder()
                .firstName("fname2")
                .lastName("lname2")
                .email("user2.test@gmail.com")
                .roles(List.of(roleUser))
                .password("pass")
                .build();
        userService.save(user2);

        mockMvc.perform(get("/users/{id}", user2.getId()).with(user(user)))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.message")
                                .value(String.format("Authenticated user's id %d and requested id %d don't match",
                                        user.getId(), user2.getId()))
                );

    }

    @Test
    void testUpdate_WithValidData_ReturnOk() throws Exception {
        User updatedUser = User
                .builder()
                .firstName("updated first name")
                .lastName("updated last name")
                .roles(new ArrayList<>())
                .build();

        String content = objectMapper.writeValueAsString(UserMapper.USER_MAPPER.userToUserDTO(updatedUser));
        mockMvc.perform(patch("/users/{id}", user.getId()).with(user(user)).content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.lastModifiedDate").exists(),
                        jsonPath("$.password").doesNotExist(),
                        jsonPath("$.roles").doesNotExist()

                );

    }

    @Test
    void testUpdate_WithInvalidId_returnBadRequest() throws Exception {
        User updatedUser = User
                .builder()
                .firstName("updated first name")
                .lastName("updated last name")
                .roles(new ArrayList<>())
                .build();
        String content = objectMapper.writeValueAsString(UserMapper.USER_MAPPER.userToUserDTO(updatedUser));
        mockMvc.perform(patch("/users/{id}", -5).with(user(user)).content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.message")
                                .value(String.format("Authenticated user's id %d and requested id %d don't match",
                                        user.getId(), -5))
                );

    }

    @Test
    void testDeleteById_WithValidId_ReturnOk() throws Exception {
        mockMvc.perform(delete("/users/{id}", user.getId()).with(user(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.password").doesNotExist(),
                        jsonPath("$.roles").doesNotExist()
                );
    }

    @Test
    void testDeleteById_WithInvalidId_ReturnBadRequest() throws Exception {
        User user2 = User
                .builder()
                .firstName("fname2")
                .lastName("lname2")
                .email("user2.test@gmail.com")
                .roles(List.of(roleUser))
                .password("pass")
                .build();
        userService.save(user2);

        mockMvc.perform(delete("/users/{id}", user2.getId()).with(user(user)))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.message")
                                .value(String.format("Authenticated user's id %d and requested id %d don't match",
                                        user.getId(), user2.getId()))
                );

    }

    @Test
    void testFindUsersByWorkspaceId_WithValidWorkspaceId_ReturnOk() throws Exception {
        Authentication authentication = new TestingAuthenticationToken(user, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user2 = User
                .builder()
                .firstName("fname2")
                .lastName("lname2")
                .email("user2.test@gmail.com")
                .roles(List.of(roleUser))
                .password("pass")
                .build();
        userService.save(user2);
        List<User> members = new ArrayList<>();
        members.add(user);
        members.add(user2);
        Workspace workspace = workspaceService.save(
                Workspace
                        .builder()
                        .name("workspace")
                        .members(members)
                        .creator(user)
                        .build()
        );

        mockMvc.perform(get("/users/workspace/{id}", workspace.getId()).with(user(user)))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$[0].password").doesNotExist(),
                        jsonPath("$[0].roles").doesNotExist(),
                        jsonPath("$[1].password").doesNotExist(),
                        jsonPath("$[1].roles").doesNotExist()
                );

    }

    @Test
    void testFindUsersByWorkspaceId_WithWorkspaceNotFound_ReturnNotFound() throws Exception {
        Authentication authentication = new TestingAuthenticationToken(user, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        mockMvc.perform(get("/users/workspace/{id}", -5).with(user(user)))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()),
                        jsonPath("$.message").exists()
                );
    }

    @Test
    void testFindUsersByTaskId_WithValidTaskId_ReturnOk() throws Exception {
        Authentication authentication = new TestingAuthenticationToken(user, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        List<User> members = new ArrayList<>();
        members.add(user);
        Workspace workspace = workspaceService.save(
                Workspace
                        .builder()
                        .name("workspace")
                        .members(members)
                        .build()
        );

        List<User> performers = new ArrayList<>();

        performers.add(user);
        Task task = Task.builder().title("test title").content("content").workspace(workspace)
                .performers(performers).build();
        taskService.save(task);

        mockMvc.perform(get("/users/task/{id}", task.getId())
                        .with(user(user)))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$").exists(),
                        jsonPath("$.size()").value(1),
                        jsonPath("$[0].email").value(user.getEmail()),
                        jsonPath("$[0].password").doesNotExist()
                );

    }

    @Test
    void testFindUsersByTaskId_WithInvalidTaskId_ReturnUnAuthorized() throws Exception {
        Authentication authentication = new TestingAuthenticationToken(user, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        List<User> members = new ArrayList<>();
        members.add(user);
        workspaceService.save(
                Workspace
                        .builder()
                        .name("workspace")
                        .members(members)
                        .build()
        );

        mockMvc.perform(get("/users/task/{id}", 0)
                        .with(user(user)))
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()),
                        jsonPath("$.message").exists()
                );

    }

}
