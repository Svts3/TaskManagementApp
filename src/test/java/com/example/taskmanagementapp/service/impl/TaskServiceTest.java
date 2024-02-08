package com.example.taskmanagementapp.service.impl;

import com.example.taskmanagementapp.exception.TaskNotFoundException;
import com.example.taskmanagementapp.exception.UserNotFoundException;
import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import com.example.taskmanagementapp.repository.TaskRepository;

import static org.junit.jupiter.api.Assertions.*;

import com.example.taskmanagementapp.repository.UserRepository;
import com.example.taskmanagementapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Mock
    private UserRepository userRepository;

    private Task task;

    private Workspace workspace;

    private User user;
    private User user2;

    @BeforeEach
    void setUp() {
        workspace = Workspace.builder().id(1L).build();
        user = User.builder().id(1L).workspaces(List.of(workspace)).build();
        user2 = User.builder().id(2L).workspaces(List.of(workspace)).build();
        List<User> performers = new ArrayList<>();
        task = Task
                .builder()
                .id(1L)
                .title("title")
                .content("content")
                .creator(User.builder().build())
                .deadlineDate(new Date(System.currentTimeMillis() + 86000000))
                .workspace(workspace)
                .performers(performers)
                .status("to do")
                .build();
    }

    @Test
    void testSaveTask() {
        when(taskRepository.save(task)).thenReturn(task);

        Task savedTask = taskService.save(task);

        assertNotNull(savedTask);
        assertNotNull(savedTask.getCreator());
        assertNotNull(savedTask.getDeadlineDate());
        assertNotNull(savedTask.getCreationDate());
        assertEquals(1L, savedTask.getId());
        assertEquals("title", savedTask.getTitle());
        assertEquals("content", savedTask.getContent());
        assertEquals("to do", savedTask.getStatus());

        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testFindTaskById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task foundTask = taskService.findById(1L);

        assertNotNull(foundTask);
        assertEquals(1L, foundTask.getId());
        assertEquals("title", foundTask.getTitle());
        assertEquals("content", foundTask.getContent());
        assertEquals("to do", foundTask.getStatus());

        verify(taskRepository, times(1)).findById(1L);
    }


    @Test
    void testFindById_NotFoundThrowTaskNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(TaskNotFoundException.class, () -> taskService.findById(1L));
        assertEquals("Task with 1 ID was not found!", exception.getMessage());

    }

    @Test
    void testFindAll() {
        Task task2 = Task.builder().title("title2").content("content2").build();
        when(taskRepository.findAll()).thenReturn(List.of(task, task2));
        List<Task> foundTasks = taskService.findAll();

        assertNotNull(foundTasks);
        assertEquals(2, foundTasks.size());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void testUpdateTask() {
        Task task2 = Task.builder().title("title2").content("content2").build();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        Task updatedTask = taskService.update(task2, 1L);

        assertNotNull(updatedTask);
        assertEquals("title2", updatedTask.getTitle());
        assertEquals("content2", updatedTask.getContent());
        assertEquals("to do", updatedTask.getStatus());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testUpdate_NotFoundThrowTaskNotFoundException() {
        Task task2 = Task.builder().title("title2").content("content2").build();
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(TaskNotFoundException.class, () -> taskService.update(task2, 1L));
        assertEquals("Task with 1 ID was not found!", exception.getMessage());
    }

    @Test
    void testDeleteById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        Task deletedTask = taskService.deleteById(1L);
        assertNotNull(deletedTask);
        assertEquals("title", deletedTask.getTitle());
        assertEquals("content", deletedTask.getContent());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteById_NotFoundThrowTaskNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(TaskNotFoundException.class, () -> taskService.deleteById(1L));
        assertEquals("Task with 1 ID was not found!", exception.getMessage());
    }

    @Test
    void testFindByWorkspaceId() {
        when(taskRepository.findByWorkspaceId(1L)).thenReturn(List.of(task));
        List<Task> tasks = taskService.findByWorkspaceId(1L);
        assertNotNull(task);
        assertEquals(1, tasks.size());
        verify(taskRepository, times(1)).findByWorkspaceId(1L);
    }

    @Test
    void testAddPerformersToTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(taskRepository.save(task)).thenReturn(task);

        Task updatedTaskTest = taskService.addPerformersToTask(1L, List.of(1L, 2L));

        assertNotNull(updatedTaskTest);
        assertEquals(2, updatedTaskTest.getPerformers().size());
        verify(taskRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testAddPerformersToTask_UserNotFound_ThrowUserNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(taskRepository.save(task)).thenReturn(task);
        RuntimeException exception = assertThrows(UserNotFoundException.class,
                () -> taskService.addPerformersToTask(1L, List.of(1L, 2L)));
        assertEquals("User with 1 ID was not found!", exception.getMessage());
    }

    @Test
    void testAddPerformersToTask_TaskNotFound_ThrowTaskNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(task)).thenReturn(task);
        RuntimeException exception = assertThrows(TaskNotFoundException.class,
                () -> taskService.addPerformersToTask(1L, List.of(1L)));
        assertEquals("Task with 1 ID was not found!", exception.getMessage());
    }

    @Test
    void testAddPerformersToTask_UsersNotInWorkspace_ThrowRuntimeException() {
        user.setWorkspaces(List.of());
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(task)).thenReturn(task);
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskService.addPerformersToTask(1L, List.of(1L)));
        assertEquals("Users are not in workspace!", exception.getMessage());
    }

    @Test
    void testRemovePerformerFromTask() {
        task.getPerformers().add(user);
        assertEquals(1, task.getPerformers().size());
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        Task updatedTask = taskService.removePerformerFromTask(1L, 1L);
        assertNotNull(updatedTask);
        assertEquals(0, updatedTask.getPerformers().size());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(task);

    }

    @Test
    void testRemovePerformerFromTask_TaskNotFound_ThrowTaskNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        when(taskRepository.save(task)).thenReturn(task);
        RuntimeException exception = assertThrows(TaskNotFoundException.class,
                ()->taskService.removePerformerFromTask(1L, 1L));
    }


}
