package com.example.taskmanagementapp.service.impl;

import com.example.taskmanagementapp.exception.UserNotInWorkspaceException;
import com.example.taskmanagementapp.exception.WorkspaceNotFoundException;
import com.example.taskmanagementapp.model.Role;
import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import com.example.taskmanagementapp.repository.WorkspaceRepository;
import com.example.taskmanagementapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkspaceServiceTest {

    @Mock
    WorkspaceRepository workspaceRepository;

    @InjectMocks
    WorkspaceServiceImpl workspaceService;

    @Mock
    JdbcMutableAclService jdbcMutableAclService;

    @Mock
    UserService userService;

    Workspace workspace;

    static final String WORKSPACE_NOT_FOUND_EXCEPTION_MESSAGE = "Workspace with %d ID was not found!";

    static final String WORKSPACE_BY_TASK_NOT_FOUND_EXCEPTION_MESSAGE = "Workspace by task id %d was not found!";

    User user;

    Authentication authentication;

    MutableAcl acl;

    @BeforeEach
    void setUp() {
        // Create user first
        user = User
                .builder()
                .id(1L)
                .email("test.test@gmail.com")
                .password("pass")
                .workspaces(new ArrayList<>())
                .roles(List.of(new Role(1L, "ROLE_ADMIN"))).build();

        // Create list of members including the test user
        List<User> members = new ArrayList<>();
        members.add(user);  // Add user with ID 1
        members.add(User.builder().id(2L).build());

        workspace = Workspace
                .builder()
                .id(1L)
                .name("workspace")
                .tasks(List.of(Task.builder().id(1L).title("title").workspace(workspace).content("content").creationDate(new Date()).build()))
                .creator(new User())
                .creationDate(new Date())
                .members(members)
                .build();

        authentication = new TestingAuthenticationToken(user, null, "ROLE_ADMIN");

        SecurityContextHolder.getContext().setAuthentication(authentication);

        acl = new AclImpl(new ObjectIdentityImpl(workspace), new PrincipalSid(user.getEmail()),
                new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority("ROLE_ADMIN")),
                new ConsoleAuditLogger());
    }

    @Test
    void testSave_WithValidWorkspace_SaveSuccessfully() {
        when(jdbcMutableAclService.createAcl(new ObjectIdentityImpl(workspace))).thenReturn(acl);

        when(workspaceRepository.save(workspace)).thenReturn(workspace);

        Workspace savedWorkspace = workspaceService.save(workspace);

        assertNotNull(savedWorkspace);
        assertNotNull(savedWorkspace.getCreator());
        assertNotNull(savedWorkspace.getCreationDate());

        assertEquals(1L, savedWorkspace.getId());
        assertEquals("workspace", savedWorkspace.getName());
        assertEquals(1, savedWorkspace.getTasks().size());
        assertEquals(1, savedWorkspace.getMembers().size());
    }

    @Test
    void findById_WithValidId_ReturnValidWorkspace() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        Workspace foundWorkspace = workspaceService.findById(1L);
        assertNotNull(foundWorkspace);
        assertSame(workspace, foundWorkspace);
    }

    @Test
    void findById_WithInvalidId_ThrowWorkspaceNotFoundException() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(WorkspaceNotFoundException.class, () -> workspaceService.findById(1L));
        assertEquals(String.format(WORKSPACE_NOT_FOUND_EXCEPTION_MESSAGE, 1L), exception.getMessage());
    }

    @Test
    void testFindAll_ReturnAllWorkspaces() {
        Workspace workspace2 = new Workspace();
        when(workspaceRepository.findAll()).thenReturn(List.of(workspace, workspace2));
        List<Workspace> foundWorkspaces = workspaceService.findAll();
        assertNotNull(foundWorkspaces);
        assertEquals(2, foundWorkspaces.size());
    }

    @Test
    void testUpdate_WithValidData_UpdateSuccessfully() {
        // Create an update object with only name and date - explicitly set empty tasks and members collections
        Workspace workspace2 = Workspace
                .builder()
                .name("workspaceUpd")
                .lastModifiedDate(new Date())
                // Not setting tasks or members to simulate a partial update
                .build();

        // Create a mutable copy of workspace with mutable collections
        Workspace mutableWorkspace = Workspace.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .creationDate(workspace.getCreationDate())
                .lastModifiedDate(workspace.getLastModifiedDate())
                .creator(workspace.getCreator())
                .members(new ArrayList<>(workspace.getMembers()))
                .tasks(new ArrayList<>(workspace.getTasks()))
                .build();

        // Verify the initial state has the expected tasks
        assertEquals(1, mutableWorkspace.getTasks().size());
        assertEquals(2, mutableWorkspace.getMembers().size());

        // Set up mock behavior - the workspaceRepository should return our mutable workspace
        // and when save is called, it should return the same mutable workspace (simulating persistence)
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(mutableWorkspace));
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(invocation -> {
            // This will return the actual modified workspace that was passed to save
            return invocation.getArgument(0);
        });

        Workspace updatedWorkspace = workspaceService.update(workspace2, 1L);

        assertNotNull(updatedWorkspace);
        assertNotNull(updatedWorkspace.getLastModifiedDate());
        assertEquals(1L, updatedWorkspace.getId());
        assertEquals("workspaceUpd", updatedWorkspace.getName());

        // Tasks should be preserved since we didn't specify any in the update
        assertEquals(1, updatedWorkspace.getTasks().size());
        // Members should be preserved since we didn't specify any in the update
        assertEquals(2, updatedWorkspace.getMembers().size());
    }

    @Test
    void testUpdate_WithInvalidWorkspaceId_ThrowWorkspaceNotFoundException() {
        Workspace workspace2 = Workspace.builder().name("workspaceUpd").build();
        when(workspaceRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(WorkspaceNotFoundException.class, () -> workspaceService.update(workspace2, 1L));
        assertEquals(String.format(WORKSPACE_NOT_FOUND_EXCEPTION_MESSAGE, 1L), exception.getMessage());
    }

    @Test
    void testDeleteById_WithValidId_DeleteSuccessfully() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        Workspace deletedWorkspace = workspaceService.deleteById(1L);
        assertNotNull(deletedWorkspace);
        assertSame(workspace, deletedWorkspace);
    }

    @Test
    void testDeleteById_WithInvalidId_ThrowWorkspaceNotFoundException() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(WorkspaceNotFoundException.class, () -> workspaceService.deleteById(1L));
        assertEquals(String.format(WORKSPACE_NOT_FOUND_EXCEPTION_MESSAGE, 1L), exception.getMessage());
    }

    @Test
    void testAddUsersToWorkspace_WithValidData_AddUsersToWorkspaceSuccessfully() {
        // Create a different user that isn't already in members list
        User newUser = User.builder().id(3L).email("new.user@gmail.com").workspaces(new ArrayList<>()).build();

        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(userService.findById(3L)).thenReturn(newUser);
        when(jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace))).thenReturn(acl);
        when(workspaceRepository.save(workspace)).thenReturn(workspace);

        Workspace updatedWorkspace = workspaceService.addUsersToWorkspace(1L, List.of(3L));
        assertNotNull(updatedWorkspace);
        assertEquals(3, updatedWorkspace.getMembers().size()); // Now expecting 3 members (2 existing + 1 new)
    }

    @Test
    void testAddUsersToWorkspace_WithInvalidWorkspaceId_ThrowWorkspaceNotFoundException() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(WorkspaceNotFoundException.class,
                () -> workspaceService.addUsersToWorkspace(1L, List.of(1L)));
        assertEquals(String.format(WORKSPACE_NOT_FOUND_EXCEPTION_MESSAGE, 1L), exception.getMessage());
    }

    @Test
    void testRemoveUsersFromWorkspace_WithValidData_RemoveUsersSuccessfully() {
        User user2 = User.builder().id(2L).email("test2.test@gmail.com").password("pass")
                .roles(List.of(new Role(1L, "ROLE_USER"))).build();

        ArrayList<User> members = new ArrayList<>();
        members.add(user2);
        workspace.setMembers(members);
        when(jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace))).thenReturn(acl);
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(userService.findById(2L)).thenReturn(user2);
        when(workspaceRepository.save(workspace)).thenReturn(workspace);
        acl.getEntries().add(new AccessControlEntryImpl(1L, acl,
                new PrincipalSid(user.getEmail()), BasePermission.ADMINISTRATION,
                true, true, true));

        Workspace updatedWorkspace = workspaceService.removeUserFromWorkspace(1L, 2L);
        assertNotNull(updatedWorkspace);
        assertEquals(0, updatedWorkspace.getMembers().size());
    }

    @Test
    void testRemoveUsersFromWorkspace_WithUserNotInWorkspace_ThrowUserNotInWorkspaceException() {
        // Create a workspace without any members for this test
        Workspace emptyWorkspace = Workspace
                .builder()
                .id(3L)
                .name("empty workspace")
                .members(new ArrayList<>())
                .build();

        User nonMemberUser = User.builder().id(5L).email("nonmember@test.com").build();

        when(workspaceRepository.findById(3L)).thenReturn(Optional.of(emptyWorkspace));
        when(userService.findById(5L)).thenReturn(nonMemberUser);

        assertThrows(UserNotInWorkspaceException.class,
                ()->workspaceService.removeUserFromWorkspace(3L, 5L));
    }


    @Test
    void testFindByTasksId_WithValidTaskId_ReturnValidTask() {
        when(workspaceRepository.findByTasksId(1L)).thenReturn(Optional.of(workspace));
        Workspace workspace1 = workspaceService.findByTasksId(1L);

        assertNotNull(workspace1);
        assertTrue(() -> workspace1.getTasks().stream().anyMatch(task -> task.getId().equals(1L)));
    }

    @Test
    void testFindByTasksId_WithInvalidWorkspaceId_ThrowWorkspaceNotFoundException() {
        when(workspaceRepository.findByTasksId(1L)).thenReturn(Optional.empty());
        WorkspaceNotFoundException exception =
                assertThrows(WorkspaceNotFoundException.class, () -> workspaceService.findByTasksId(1L));
        assertEquals(String.format(WORKSPACE_BY_TASK_NOT_FOUND_EXCEPTION_MESSAGE, 1), exception.getMessage());
    }

    @Test
    void testAddPermissionToUserInWorkspace_WithValidData_AddPermissionSuccessfully() {
        // The workspace members list now includes user with ID 1 from setUp
        when(userService.findById(user.getId())).thenReturn(user);
        when(workspaceRepository.findById(workspace.getId())).thenReturn(Optional.of(workspace));
        when(jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace))).thenReturn(acl);

        workspaceService.addPermissionsForUserInWorkspace(workspace.getId(), user.getId(), List.of("WRITE", "DELETE"));

        assertEquals(2, acl.getEntries().size());
        assertEquals(new PrincipalSid(user.getEmail()), acl.getEntries().get(0).getSid());
        assertEquals(new PrincipalSid(user.getEmail()), acl.getEntries().get(1).getSid());
        assertEquals(BasePermission.WRITE, acl.getEntries().get(0).getPermission());
        assertEquals(BasePermission.DELETE, acl.getEntries().get(1).getPermission());
        verify(jdbcMutableAclService, times(1)).readAclById(new ObjectIdentityImpl(workspace));
        verify(userService, times(1)).findById(user.getId());
        verify(workspaceRepository, times(1)).findById(workspace.getId());
    }

    @Test
    void testAddPermissionToUserInWorkspace_WithLowerCase_AddPermissionSuccessfully() {
        // The workspace members list now includes user with ID 1 from setUp
        when(userService.findById(user.getId())).thenReturn(user);
        when(workspaceRepository.findById(workspace.getId())).thenReturn(Optional.of(workspace));
        when(jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace))).thenReturn(acl);

        workspaceService.addPermissionsForUserInWorkspace(workspace.getId(), user.getId(), List.of("write", "delete"));

        assertEquals(2, acl.getEntries().size());
        assertEquals(new PrincipalSid(user.getEmail()), acl.getEntries().get(0).getSid());
        assertEquals(new PrincipalSid(user.getEmail()), acl.getEntries().get(1).getSid());
        assertEquals(BasePermission.WRITE, acl.getEntries().get(0).getPermission());
        assertEquals(BasePermission.DELETE, acl.getEntries().get(1).getPermission());
        verify(jdbcMutableAclService, times(1)).readAclById(new ObjectIdentityImpl(workspace));
        verify(userService, times(1)).findById(user.getId());
        verify(workspaceRepository, times(1)).findById(workspace.getId());
    }

    @Test
    void testAddPermissionToUserInWorkspace_WithInvalidWorkspaceId_ThrowWorkspaceNotFoundException() {
        when(workspaceRepository.findById(workspace.getId())).thenReturn(Optional.empty());
        WorkspaceNotFoundException exception = assertThrows(WorkspaceNotFoundException.class,
                () -> workspaceService.addPermissionsForUserInWorkspace(workspace.getId(), user.getId(), List.of("WRITE")));
        assertEquals(String.format(WORKSPACE_NOT_FOUND_EXCEPTION_MESSAGE, workspace.getId()), exception.getMessage());
    }

    @Test
    void testRemovePermissionsForUserInWorkspace_WithValidData_RemovePermissionsForUserSuccessfully() {
        // The workspace members list now includes user with ID 1 from setUp
        when(userService.findById(user.getId())).thenReturn(user);
        when(workspaceRepository.findById(workspace.getId())).thenReturn(Optional.of(workspace));
        when(jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace))).thenReturn(acl);

        // Add permissions that we'll later remove
        acl.insertAce(acl.getEntries().size(), BasePermission.DELETE, new PrincipalSid(user.getEmail()), true);
        acl.insertAce(acl.getEntries().size(), BasePermission.ADMINISTRATION, new PrincipalSid(user.getEmail()), true);

        workspaceService.removePermissionsForUserInWorkspace(workspace.getId(), user.getId(), List.of("ADMIN"));

        assertEquals(1, acl.getEntries().size());
        assertTrue(acl.getEntries().stream().anyMatch(entry -> entry.getPermission().equals(BasePermission.DELETE)));
        verify(jdbcMutableAclService, times(1)).readAclById(new ObjectIdentityImpl(workspace));
        verify(userService, times(1)).findById(user.getId());
        verify(workspaceRepository, times(1)).findById(workspace.getId());
    }

    @Test
    void testRemovePermissionsForUserInWorkspace_WithInvalidWorkspaceId_ThrowWorkspaceNotFoundException() {
        when(workspaceRepository.findById(workspace.getId())).thenReturn(Optional.empty());
        WorkspaceNotFoundException exception = assertThrows(WorkspaceNotFoundException.class,
                () -> workspaceService.removePermissionsForUserInWorkspace(workspace.getId(),
                        user.getId(), List.of("DELETE")));

        assertEquals(String.format(WORKSPACE_NOT_FOUND_EXCEPTION_MESSAGE, workspace.getId()),
                exception.getMessage());

    }
}
