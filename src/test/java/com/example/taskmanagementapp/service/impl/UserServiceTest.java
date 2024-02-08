package com.example.taskmanagementapp.service.impl;

import com.example.taskmanagementapp.exception.UserNotFoundException;
import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import com.example.taskmanagementapp.repository.UserRepository;
import com.example.taskmanagementapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;


    @BeforeEach
    void setUp() {
        user = User
                .builder()
                .id(1L)
                .firstName("firstName")
                .lastName("lastName")
                .email("email")
                .password("pass")
                .build();
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
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testFindById() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User user1 = userService.findById(1L);
        assertNotNull(user1);
        assertEquals("email", user1.getEmail());
        assertEquals("firstName", user1.getFirstName());
        assertEquals("lastName", user1.getLastName());
        verify(userRepository, times(1)).findById(1L);
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
        User user2 = User
                .builder()
                .id(2L)
                .firstName("firstName2")
                .lastName("lastName2")
                .email("email2")
                .password("pass2")
                .build();
        when(userRepository.findAll()).thenReturn(List.of(user, user2));
        List<User> users = userService.findAll();
        assertNotNull(users);
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testUpdateUser() throws Exception {
        User user1 = User
                .builder()
                .firstName("firstNameUpd")
                .lastName("LastNameUpd")
                .email("emailUpd")
                .password("passwordUpd")
                .workspaces(List.of(new Workspace()))
                .tasks(List.of(new Task()))
                .build();

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
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUser_ThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.update(new User(), 1L);
        });
        assertEquals("User with 1 ID was not found!", exception.getMessage());
    }


    @Test
    void testDeleteById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User deletedUser = userService.deleteById(1L);
        assertNotNull(deletedUser);
        assertEquals("email", deletedUser.getEmail());
        assertEquals("firstName", deletedUser.getFirstName());
        assertEquals("lastName", deletedUser.getLastName());
        verify(userRepository, times(1)).findById(1L);
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
        verify(userRepository, times(1)).findByEmail("email");
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
        verify(userRepository, times(1)).existsByEmail("email");
    }

    @Test
    void testExistsByEmail_NotFound() {
        when(userRepository.existsByEmail("testEmail")).thenReturn(false);
        Boolean result = userService.existsByEmail("testEmail");
        assertFalse(result);
        verify(userRepository, times(1)).existsByEmail("testEmail");
    }

    @Test
    void testFindUsersByTaskId() {
        user.setTasks(List.of(Task.builder().id(1L).title("title").content("task").build()));
        when(userRepository.findUsersByTasksId(1L)).thenReturn(List.of(user));
        List<User> foundUsers = userService.findUsersByTasksId(1L);

        assertNotNull(foundUsers);
        assertEquals(1, foundUsers.size());
        assertEquals("firstName", foundUsers.get(0).getFirstName());
        verify(userRepository, times(1)).findUsersByTasksId(1L);
    }

    @Test
    void testFindUsersByWorkspaceId() {
        user.setWorkspaces(List.of(Workspace.builder().id(1L).name("workspace").build()));
        when(userRepository.findUsersByWorkspacesId(1L)).thenReturn(List.of(user));
        List<User> foundUsers = userService.findUsersByWorkspacesId(1L);

        assertNotNull(foundUsers);
        assertEquals(1, foundUsers.size());
        assertEquals("firstName", foundUsers.get(0).getFirstName());
        verify(userRepository, times(1)).findUsersByWorkspacesId(1L);
    }


}
