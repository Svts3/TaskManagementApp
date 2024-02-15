package com.example.taskmanagementapp.service.impl;

import com.example.taskmanagementapp.exception.UserNotFoundException;
import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import com.example.taskmanagementapp.repository.UserRepository;
import com.example.taskmanagementapp.service.UserService;
import org.hibernate.jdbc.Work;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    private Workspace workspace;


    @BeforeEach
    void setUp() {
        List<User> members = new ArrayList<>();
        members.add(user);
        workspace = Workspace.builder().id(1L).name("test").creator(user).members(members).build();
        List<Workspace> userWorkspaces = new ArrayList<>();
        userWorkspaces.add(workspace);
        user = User.builder().id(1L).firstName("firstName").lastName("lastName").email("email").password("pass").workspaces(userWorkspaces).build();
    }


    @Test
    void testSave() {
        when(userRepository.save(user)).thenReturn(user);
        User user1 = userService.save(user);
        assertNotNull(user1);
        assertNotNull(user1.getCreationDate());
        assertEquals("email", user1.getEmail());
        assertEquals("firstName", user1.getFirstName());
        assertEquals("lastName", user1.getLastName());
    }

    @Test
    void testFindById() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User user1 = userService.findById(1L);
        assertNotNull(user1);
        assertEquals("email", user1.getEmail());
        assertEquals("firstName", user1.getFirstName());
        assertEquals("lastName", user1.getLastName());
    }

    @Test
    void testFindById_ThrowException() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.findById(1L);
        });
        assertEquals("User with 1 ID was not found!", exception.getMessage());
    }

    @Test
    void testFindAll() {
        User user2 = User.builder().id(2L).firstName("firstName2").lastName("lastName2").email("email2").password("pass2").build();
        when(userRepository.findAll()).thenReturn(List.of(user, user2));
        List<User> users = userService.findAll();
        assertNotNull(users);
        assertEquals(2, users.size());
    }

    @Test
    void testUpdateUser() throws Exception {
        User user1 = User.builder().firstName("firstNameUpd").lastName("LastNameUpd").email("emailUpd").password("passwordUpd").workspaces(List.of(new Workspace())).tasks(List.of(new Task())).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User updatedUser = userService.update(user1, 1L);

        assertNotNull(updatedUser);
        assertNotNull(updatedUser.getWorkspaces());
        assertNotNull(updatedUser.getTasks());

        assertEquals(1, updatedUser.getWorkspaces().size());
        assertEquals(1, updatedUser.getTasks().size());

        assertEquals("firstNameUpd", updatedUser.getFirstName());
        assertEquals("LastNameUpd", updatedUser.getLastName());
        assertEquals("emailUpd", updatedUser.getEmail());
        assertEquals("passwordUpd", updatedUser.getPassword());
        assertEquals("firstNameUpd", updatedUser.getFirstName());
    }

    @Test
    void testUpdateUser_ThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.update(new User(), 1L);
        });
    }


    @Test
    void testDeleteById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User deletedUser = userService.deleteById(1L);
        assertNotNull(deletedUser);
        assertEquals("email", deletedUser.getEmail());
        assertEquals("firstName", deletedUser.getFirstName());
        assertEquals("lastName", deletedUser.getLastName());
    }

    @Test
    void testDeleteById_ThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(UserNotFoundException.class, () -> userService.deleteById(1L));
        assertEquals("User with 1 ID was not found!", exception.getMessage());

    }

    @Test
    void testFindByEmail() {
        when(userRepository.findByEmail("email")).thenReturn(Optional.of(user));
        User foundUser = userService.findByEmail("email");
        assertNotNull(foundUser);
        assertEquals("email", foundUser.getEmail());
    }

    @Test
    void testFindByEmail_ThrowException() {
        when(userRepository.findByEmail("email")).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(UserNotFoundException.class, () -> userService.findByEmail("email"));
        assertEquals("User with email email was not found!", exception.getMessage());
    }

    @Test
    void testExistsByEmail() {
        when(userRepository.existsByEmail("email")).thenReturn(true);
        Boolean result = userService.existsByEmail("email");
        assertTrue(result);
    }

    @Test
    void testExistsByEmail_NotFound() {
        when(userRepository.existsByEmail("testEmail")).thenReturn(false);
        Boolean result = userService.existsByEmail("testEmail");
        assertFalse(result);
    }

    @Test
    void testFindUsersByTaskId() {
        user.setTasks(List.of(Task.builder().id(1L).title("title").content("task").build()));
        when(userRepository.findUsersByTasksId(1L)).thenReturn(List.of(user));
        List<User> foundUsers = userService.findUsersByTasksId(1L);

        assertNotNull(foundUsers);
        assertEquals(1, foundUsers.size());
        assertEquals("firstName", foundUsers.get(0).getFirstName());
    }

    @Test
    void testFindUsersByWorkspaceId() {
        user.setWorkspaces(List.of(Workspace.builder().id(1L).name("workspace").build()));
        when(userRepository.findUsersByWorkspacesId(1L)).thenReturn(List.of(user));
        List<User> foundUsers = userService.findUsersByWorkspacesId(1L);

        assertNotNull(foundUsers);
        assertEquals(1, foundUsers.size());
        assertEquals("firstName", foundUsers.get(0).getFirstName());
    }

    @Test
    void testExistsByEmailAndWorkspacesId_UserExists() {
        when(userRepository.existsByEmailAndWorkspacesId(user.getEmail(), 1L)).thenReturn(true);
        Boolean result = userService.existsByEmailAndWorkspacesId(user.getEmail(), 1L);
        assertTrue(result);
    }

    @Test
    void testExistsByEmailAndWorkspacesId_userDoesntExists() {
        when(userRepository.existsByEmailAndWorkspacesId(user.getEmail(), 1L)).thenReturn(false);
        Boolean result = userService.existsByEmailAndWorkspacesId(user.getEmail(), 1L);
        assertFalse(result);
    }


}
