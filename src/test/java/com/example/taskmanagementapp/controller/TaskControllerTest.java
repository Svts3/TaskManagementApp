package com.example.taskmanagementapp.controller;

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
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
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
public class TaskControllerTest {

    MockMvc mockMvc;

    @Autowired
    FilterChainProxy filterChainProxy;

    @Autowired
    TaskController taskController;

    @Autowired
    UserService userService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    WorkspaceService workspaceService;

    @Autowired
    TaskService taskService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JdbcMutableAclService mutableAcl;
    User user;

    Workspace workspace;

    private Role roleUser;
    private Role roleAdmin;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>(
            "mysql"
    );

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(taskController)
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
                .workspaces(new ArrayList<>())
                .roles(new ArrayList<>(List.of(roleUser)))
                .build();
        userService.save(user);
        Authentication authentication = new TestingAuthenticationToken(user, null, "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        workspace = Workspace.builder().name("test").build();
        workspace.setTasks(new ArrayList<>());
        workspace.setMembers(new ArrayList<>());
        workspace = workspaceService.save(workspace);

    }


    @Test
    void testSaveTask_WithValidData_ReturnOk() throws Exception {
        Task taskToSave = Task.builder().title("test")
                .content("test")
                .workspace(Workspace.builder().id(workspace.getId()).build()).build();
        mockMvc.perform(post("/tasks/")
                .content(objectMapper.writeValueAsString(taskToSave))
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(user))).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.creator.id").value(user.getId()),
                jsonPath("$.creationDate").exists(),
                jsonPath("$.lastModifiedDate").exists()
        );
    }

    @Test
    void testFindAll_WithValidData_ReturnOk() throws Exception {
        Task taskToSave = Task.builder().title("test")
                .content("test")
                .workspace(Workspace.builder()
                        .id(workspace.getId()).build()).build();
        taskService.save(taskToSave);

        mockMvc.perform(get("/tasks/").with(user(user)))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.size()").value(1)
                );
    }

    @Test
    void testFindByWorkspaceId_WithValidId_ReturnOk() throws Exception {
        Task taskToSave = Task.builder().title("test")
                .content("test")
                .workspace(Workspace.builder()
                        .id(workspace.getId()).build()).build();
        taskService.save(taskToSave);
        mockMvc.perform(get("/tasks/workspace/{id}", workspace.getId())
                        .with(user(user)))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.size()").value(1)
                );

    }

    @Test
    void testFindByWorkspaceId_WithInvalidId_ReturnUnauthorized() throws Exception {
        Task taskToSave = Task.builder().title("test")
                .content("test")
                .workspace(Workspace.builder()
                        .id(workspace.getId()).build()).build();
        taskService.save(taskToSave);
        mockMvc.perform(get("/tasks/workspace/{id}", -5)
                        .with(user(user)))
                .andExpectAll(
                        status().isUnauthorized(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.statusCode").value(HttpStatus.UNAUTHORIZED.value()),
                        jsonPath("$.message").exists()
                );

    }

    @Test
    void testFindById_WithValidId_ReturnOk() throws Exception {
        Task task = Task.builder().title("test")
                .content("test")
                .workspace(Workspace.builder()
                        .id(workspace.getId()).build()).build();
        task = taskService.save(task);

        mockMvc.perform(get("/tasks/{id}", task.getId()).with(user(user)))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.creator.id").value(user.getId())
                );
    }

    @Test
    void testFindById_WithInvalidId_ReturnNotFound() throws Exception {
        Task task = Task.builder().title("test")
                .content("test")
                .workspace(Workspace.builder()
                        .id(workspace.getId()).build()).build();
        task = taskService.save(task);

        mockMvc.perform(get("/tasks/{id}", -5).with(user(user)))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()),
                        jsonPath("$.message").exists()
                );

    }


    @Test
    void testUpdate_WithValidData_ReturnOk() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MutableAcl acl = (MutableAcl) mutableAcl.readAclById(new ObjectIdentityImpl(workspace));
        acl.insertAce(acl.getEntries().size(), BasePermission.WRITE, new PrincipalSid(authentication), true);

        Task task = Task.builder().title("test")
                .content("test")
                .workspace(Workspace.builder()
                        .id(workspace.getId()).build()).build();
        task = taskService.save(task);
        workspace.getTasks().add(task);
        workspaceService.update(workspace, workspace.getId());

        Task newTask = Task.builder().title("updated title").content("updated content").build();
        mockMvc.perform(patch("/tasks/{id}", task.getId()).with(user(user))
                        .content(objectMapper.writeValueAsString(newTask)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.title").value("updated title"),
                        jsonPath("$.content").value("updated content")
                );


    }

    @Test
    void testUpdate_WithInvalidId_ReturnUnauthorized() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MutableAcl acl = (MutableAcl) mutableAcl.readAclById(new ObjectIdentityImpl(workspace));
        acl.insertAce(acl.getEntries().size(), BasePermission.WRITE, new PrincipalSid(authentication), true);

        Task newTask = Task.builder().title("updated title").content("updated content").build();
        mockMvc.perform(patch("/tasks/{id}", -5).with(user(user))
                        .content(objectMapper.writeValueAsString(newTask)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()),
                        jsonPath("$.message").exists()
                );
    }

    @Test
    void testAddPerformersToTask_WithValidData_ReturnOk() throws Exception {
        Task task = Task.builder().title("test")
                .content("test")
                .workspace(workspace)
                .performers(new ArrayList<>())
                .build();
        task = taskService.save(task);
        User performer = User
                .builder()
                .firstName("fName2")
                .lastName("lName2")
                .email("test2.test@gmail.com")
                .password("pass")
                .workspaces(new ArrayList<>())
                .build();

        performer = userService.save(performer);
        Workspace workspace1 = workspaceService.addUsersToWorkspace(workspace.getId(), List.of(performer.getId()));
        mockMvc.perform(patch("/tasks/{id}/users", task.getId()).with(user(user))
                .content(objectMapper.writeValueAsString(List.of(performer.getId())))
                .contentType(MediaType.APPLICATION_JSON)).andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.performers[0].email").value(performer.getEmail())
        );

    }

    @Test
    void testAddPerformersToTask_WithInvalidTaskId_ReturnNotFound() throws Exception {
        Task task = Task.builder().title("test")
                .content("test")
                .workspace(Workspace.builder()
                        .id(workspace.getId()).build())
                .build();
        taskService.save(task);

        User performer = User
                .builder()
                .firstName("fName2")
                .lastName("lName2")
                .email("test2.test@gmail.com")
                .password("pass")
                .roles(List.of(roleUser))
                .build();
        performer = userService.save(performer);
        mockMvc.perform(patch("/tasks/{id}/users", -5).with(user(user))
                .content(objectMapper.writeValueAsString(List.of(performer.getId()))).contentType(MediaType.APPLICATION_JSON)).andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()),
                jsonPath("$.message").exists()
        );

    }

    @Test
    void testAddPerformersToTask_WithInvalidPerformerId_ReturnNotFound() throws Exception {
        Task task = Task.builder().title("test")
                .content("test")
                .workspace(Workspace.builder()
                        .id(workspace.getId()).build())
                .build();
        task = taskService.save(task);

        mockMvc.perform(patch("/tasks/{id}/users", task.getId()).with(user(user))
                .content(objectMapper.writeValueAsString(List.of(-5))).contentType(MediaType.APPLICATION_JSON)).andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()),
                jsonPath("$.message").exists()
        );

    }

    @Test
    void testRemovePerformerFromTask_WithValidData_ReturnOk() throws Exception {
        User performer = User
                .builder()
                .firstName("fName2")
                .lastName("lName2")
                .email("test2.test@gmail.com")
                .password("pass")
                .workspaces(new ArrayList<>())
                .build();
        Task task = Task.builder().title("test")
                .content("test")
                .workspace(workspace)
                .performers(new ArrayList<>())
                .build();
        task.getPerformers().add(performer);
        task = taskService.save(task);

        performer = userService.save(performer);
        workspaceService.addUsersToWorkspace(workspace.getId(), List.of(performer.getId()));
        mockMvc.perform(patch("/tasks/{id}/users/{userId}", task.getId(), performer.getId())
                        .with(user(user)))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON)
                );
    }

    @Test
    void testRemovePerformerFromTask_WithInvalidTaskId_ReturnNotFound() throws Exception {
        Task task = Task.builder().title("test")
                .content("test")
                .workspace(workspace)
                .performers(new ArrayList<>())
                .build();
        task = taskService.save(task);
        User performer = User
                .builder()
                .firstName("fName2")
                .lastName("lName2")
                .email("test2.test@gmail.com")
                .password("pass")
                .workspaces(new ArrayList<>())
                .build();

        performer = userService.save(performer);
        workspaceService.addUsersToWorkspace(workspace.getId(), List.of(performer.getId()));

        mockMvc.perform(patch("/tasks/{id}/users/{userId}", -5, performer.getId())
                        .with(user(user)))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()),
                        jsonPath("$.message").exists()
                );
    }

    @Test
    void testRemovePerformerFromTask_WithInvalidPerformerId_ReturnNotFound() throws Exception {
        Task task = Task.builder().title("test")
                .content("test")
                .workspace(workspace)
                .performers(new ArrayList<>())
                .build();
        task = taskService.save(task);
        User performer = User
                .builder()
                .firstName("fName2")
                .lastName("lName2")
                .email("test2.test@gmail.com")
                .password("pass")
                .workspaces(new ArrayList<>())
                .build();

        performer = userService.save(performer);
        workspaceService.addUsersToWorkspace(workspace.getId(), List.of(performer.getId()));

        mockMvc.perform(patch("/tasks/{id}/users/{userId}", task.getId(), -5)
                        .with(user(user)))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()),
                        jsonPath("$.message").exists()
                );
    }

    @Test
    void testRemovePerformerFromTask_WithPerformerNotInWorkspace_ReturnUnauthorized() throws Exception {
        Task task = Task.builder().title("test")
                .content("test")
                .workspace(workspace)
                .performers(new ArrayList<>())
                .build();
        task = taskService.save(task);
        User performer = User
                .builder()
                .firstName("fName2")
                .lastName("lName2")
                .email("test2.test@gmail.com")
                .password("pass")
                .workspaces(new ArrayList<>())
                .build();

        performer = userService.save(performer);

        mockMvc.perform(patch("/tasks/{id}/users/{userId}", task.getId(), performer.getId())
                        .with(user(user)))
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()),
                        jsonPath("$.message").exists()
                );
    }

    @Test
    void testDeleteById_WithValidId_ReturnOk() throws Exception {
        MutableAcl acl = (MutableAcl) mutableAcl.readAclById(new ObjectIdentityImpl(workspace));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        acl.insertAce(acl.getEntries().size(), BasePermission.DELETE,
                new PrincipalSid(authentication), true);

        User performer = User
                .builder()
                .firstName("fName2")
                .lastName("lName2")
                .email("test2.test@gmail.com")
                .password("pass")
                .workspaces(new ArrayList<>())
                .build();
        Task task = Task.builder().title("test")
                .content("test")
                .workspace(workspace)
                .performers(new ArrayList<>())
                .build();
        task = taskService.save(task);

        performer = userService.save(performer);
        workspaceService.addUsersToWorkspace(workspace.getId(), List.of(performer.getId()));
        mockMvc.perform(delete("/tasks/{id}", task.getId())
                        .with(user(user)))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON)
                );
    }

    @Test
    void testDeleteById_WithInvalidId_ReturnNotFound() throws Exception {
        MutableAcl acl = (MutableAcl) mutableAcl.readAclById(new ObjectIdentityImpl(workspace));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        acl.insertAce(acl.getEntries().size(), BasePermission.DELETE,
                new PrincipalSid(authentication), true);

        var aclsa = acl.getEntries();

        User performer = User
                .builder()
                .firstName("fName2")
                .lastName("lName2")
                .email("test2.test@gmail.com")
                .password("pass")
                .workspaces(new ArrayList<>())
                .build();
        Task task = Task.builder().title("test")
                .content("test")
                .workspace(workspace)
                .performers(new ArrayList<>())
                .build();
        task = taskService.save(task);

        performer = userService.save(performer);
        workspaceService.addUsersToWorkspace(workspace.getId(), List.of(performer.getId()));
        mockMvc.perform(delete("/tasks/{id}", -5)
                        .with(user(user)))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.statusCode").value(HttpStatus.NOT_FOUND.value()),
                        jsonPath("$.message").exists()
                );
    }


}
