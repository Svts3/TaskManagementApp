package com.example.taskmanagementapp.controller;

import com.example.taskmanagementapp.exception.handler.GlobalExceptionHandler;
import com.example.taskmanagementapp.model.Role;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import com.example.taskmanagementapp.repository.RoleRepository;
import com.example.taskmanagementapp.security.config.SecurityConfig;
import com.example.taskmanagementapp.service.UserService;
import com.example.taskmanagementapp.service.WorkspaceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
@TestMethodOrder(MethodOrderer.MethodName.class)
public class WorkspaceControllerTest {

    MockMvc mockMvc;

    @Autowired
    WorkspaceController workspaceController;

    @Autowired
    FilterChainProxy filterChainProxy;

    @Autowired
    WorkspaceService workspaceService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserService userService;

    @Autowired
    MutableAclService mutableAclService;

    User user;


    Role roleUser;
    Role roleAdmin;
    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>(
            "mysql"
    );

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(workspaceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .apply(springSecurity(filterChainProxy))
                .build();

        roleUser = roleRepository.save(new Role(1L, "ROLE_USER"));
        roleAdmin = roleRepository.save(new Role(2L, "ROLE_ADMIN"));

        user = User
                .builder()
                .firstName("fName")
                .lastName("lName")
                .email("test.test@gmail.com")
                .password("pass")
                .roles(List.of(roleUser))
                .build();
        userService.save(user);

        Authentication authentication = new TestingAuthenticationToken(user, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

    }

    @Test
    void testSaveWorkspace_WithValidData_ReturnOk() throws Exception {

        Workspace workspace = Workspace.builder().name("test").build();

        String content = objectMapper.writeValueAsString(workspace);
        mockMvc.perform(post("/workspaces/").with(user(user))
                        .content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.name").value("test"),
                        jsonPath("$.creator").exists(),
                        jsonPath("$.creator.id").value(user.getId())
                );

    }

    @Test
    void testSaveWorkspace_WithNullName_ReturnBadRequest() throws Exception {

        Workspace workspace = Workspace.builder().build();

        String content = objectMapper.writeValueAsString(workspace);
        mockMvc.perform(post("/workspaces/").with(user(user))
                        .content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.message").exists()
                );

    }

    @Test
    void testFindAll_ReturnOk() throws Exception {
        Workspace workspace = Workspace.builder().name("test").build();
        Workspace workspace2 = Workspace.builder().name("test2").build();
        workspaceService.save(workspace);
        workspaceService.save(workspace2);
        mockMvc.perform(get("/workspaces/").with(user(user)))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.size()").value(2)

                );

    }

    @Test
    void testFindById_WithValidId_ReturnOk() throws Exception {
        Workspace workspace = Workspace.builder().name("test").build();
        workspaceService.save(workspace);

        mockMvc.perform(get("/workspaces/{id}", workspace.getId()).with(user(user)))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$").exists()
                );
    }

    @Test
    void testFindById_WithInValidId_ReturnNotFound() throws Exception {

        mockMvc.perform(get("/workspaces/{id}", -5).with(user(user)))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()),
                        jsonPath("$.message").exists()
                );
    }

    @Test
    void testUpdate_WithValidData_ReturnOk() throws Exception {
        Workspace workspace = Workspace.builder().name("test").build();
        workspaceService.save(workspace);

        Workspace updatedWorkspace = Workspace.builder().name("updated").build();

        mockMvc.perform(patch("/workspaces/{id}", workspace.getId()).with(user(user))
                        .content(objectMapper.writeValueAsString(updatedWorkspace))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON)
                );
    }

    @Test
    void testUpdate_WithInvalidId_ReturnUnauthorized() throws Exception {

        Workspace workspace = Workspace.builder().name("test").build();
        workspaceService.save(workspace);

        Workspace updatedWorkspace = Workspace.builder().name("updated").build();

        mockMvc.perform(patch("/workspaces/{id}", -5).with(user(user))
                        .content(objectMapper.writeValueAsString(updatedWorkspace))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isUnauthorized(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()),
                        jsonPath("$.message").exists()
                );
    }

    @Test
    void testDeleteById_WithValidId_ReturnOk() throws Exception {
        Workspace workspace = Workspace.builder().name("test").build();
        workspaceService.save(workspace);
        mockMvc.perform(delete("/workspaces/{id}", workspace.getId()).with(user(user)))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON)
                );
    }

    @Test
    void testDeleteById_WithInvalidId_ReturnUnauthorized() throws Exception {
        Workspace workspace = Workspace.builder().name("test").build();
        workspaceService.save(workspace);
        workspaceService.addPermissionsForUserInWorkspace(workspace.getId(), user.getId(), List.of("ADMIN"));
        mockMvc.perform(delete("/workspaces/{id}", -5).with(user(user)))
                .andExpectAll(
                        status().isUnauthorized(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()),
                        jsonPath("$.message").exists()
                );
    }

    @Test
    void addUsersToWorkspace_WithValidData_ReturnOk() throws Exception {
        List<User> members = new ArrayList<>();
        Workspace workspace = Workspace.builder().name("test").members(members).build();
        workspaceService.save(workspace);

        User user2 = User.builder().email("test2").build();
        userService.save(user2);
        workspaceService.addPermissionsForUserInWorkspace(workspace.getId(), user.getId(), List.of("ADMIN"));

        mockMvc.perform(post("/workspaces/{id}/users", workspace.getId()).with(user(user))
                        .content(objectMapper.writeValueAsString(List.of(user2.getId()))).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON)
                );
    }

    @Test
    void addUsersToWorkspace_WithInvalidUserId_ReturnNotAuthorize() throws Exception {
        List<User> members = new ArrayList<>();
        Workspace workspace = Workspace.builder().name("test").members(members).build();
        workspaceService.save(workspace);

        User user2 = User.builder().email("test2").build();
        userService.save(user2);
        workspaceService.addPermissionsForUserInWorkspace(workspace.getId(), user.getId(), List.of("ADMIN"));

        mockMvc.perform(post("/workspaces/{id}/users", -5).with(user(user))
                        .content(objectMapper.writeValueAsString(List.of(user2.getId()))).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isUnauthorized(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()),
                        jsonPath("$.message").exists()
                );
    }

    @Test
    void addUsersToWorkspace_WithInvalidUsers_ReturnNotFound() throws Exception {
        List<User> members = new ArrayList<>();
        Workspace workspace = Workspace.builder().name("test").members(members).build();
        workspaceService.save(workspace);

        User user2 = User.builder().email("test2").build();
        userService.save(user2);
        workspaceService.addPermissionsForUserInWorkspace(workspace.getId(), user.getId(), List.of("ADMIN"));

        mockMvc.perform(post("/workspaces/{id}/users", workspace.getId()).with(user(user))
                        .content(objectMapper.writeValueAsString(List.of(-5))).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()),
                        jsonPath("$.message").exists()
                );
    }


    @Test
    void testRemoveUserFromWorkspace_WithValidData_ReturnOk() throws Exception {
        List<User> members = new ArrayList<>();
        members.add(user);
        User user2 = User.builder().email("test").build();
        Workspace workspace = Workspace.builder().name("test").members(members).build();
        userService.save(user2);
        workspaceService.save(workspace);
        workspaceService.addPermissionsForUserInWorkspace(workspace.getId(), user.getId(), List.of("ADMIN"));
        workspaceService.addUsersToWorkspace(workspace.getId(), List.of(user2.getId()));
        mockMvc.perform(patch("/workspaces/{id}/users/{userId}", workspace.getId(), user2.getId())
                .with(user(user)).contentType(MediaType.APPLICATION_JSON)).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON)
        );
    }

    @Test
    void testRemoveUserFromWorkspace_WithInvalidUserId_ReturnNotFound() throws Exception {
        List<User> members = new ArrayList<>();
        User user2 = User.builder().email("test").build();
        members.add(user2);
        Workspace workspace = Workspace.builder().name("test").members(members).build();
        workspaceService.save(workspace);
        workspaceService.addPermissionsForUserInWorkspace(workspace.getId(), user.getId(), List.of("ADMIN"));
        mockMvc.perform(patch("/workspaces/{id}/users/{userId}", workspace.getId(), -5)
                .with(user(user)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()),
                jsonPath("$.message").exists()

        );
    }

    @Test
    void testRemoveUserFromWorkspace_WithInvalidWorkspaceId_ReturnNotFound() throws Exception {
        List<User> members = new ArrayList<>();
        members.add(user);
        User user2 = User.builder().email("test").build();
        Workspace workspace = Workspace.builder().name("test").members(members).build();
        userService.save(user2);
        workspaceService.save(workspace);
        workspaceService.addPermissionsForUserInWorkspace(workspace.getId(), user.getId(), List.of("ADMIN"));
        workspaceService.addUsersToWorkspace(workspace.getId(), List.of(user2.getId()));
        mockMvc.perform(patch("/workspaces/{id}/users/{userId}", -5, user2.getId())
                .with(user(user)).contentType(MediaType.APPLICATION_JSON)).andExpectAll(
                status().isUnauthorized(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.message").exists()
        );
    }

    @Test
    void testRemoveUserFromWorkspace_WithUserNotInWorkspace_ReturnBadRequest() throws Exception {
        List<User> members = new ArrayList<>();
        User user2 = User.builder().email("test").build();
        userService.save(user2);
        Workspace workspace = Workspace.builder().name("test").members(members).build();
        workspaceService.save(workspace);
        workspaceService.addPermissionsForUserInWorkspace(workspace.getId(), user.getId(), List.of("ADMIN"));
        mockMvc.perform(patch("/workspaces/{id}/users/{userId}", workspace.getId(), user2.getId())
                .with(user(user))).andExpectAll(
                status().isBadRequest(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()),
                jsonPath("$.message").exists()

        );
    }

}
