package com.example.taskmanagementapp.dto;

import com.example.taskmanagementapp.dto.mappers.TaskMapper;
import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TaskMapperTest {


    private Task task;

    private List<Task> tasks;

    private User user;

    @BeforeEach
    void setUp() {
        tasks = new ArrayList<>();
        user = User.builder().id(1L).email("email").firstName("first_name").build();
        task = Task
                .builder()
                .id(5L)
                .creationDate(new Date())
                .title("some title")
                .content("some content")
                .creator(user)
                .build();
    }

    @Test
    void testTaskMapper() {


        TaskDTO taskDTO = TaskMapper.TASK_MAPPER.taskToTaskDTO(task);

        assertNotNull(taskDTO);
        assertEquals("some title", taskDTO.getTitle());
        assertEquals("some content", taskDTO.getContent());
        assertEquals(1L, taskDTO.getCreator().getId());
        assertEquals("email", taskDTO.getCreator().getEmail());
        assertEquals("first_name", taskDTO.getCreator().getFirstName());
        assertEquals(UserDTO.class, taskDTO.getCreator().getClass());
    }

    @Test
    void testTaskMapperWithList() {

        tasks.add(task);
        tasks.add(Task.builder().id(2L).title("title2").content("content2")
                .workspace(Workspace.builder().build())
                .creationDate(new Date())
                .performers(List.of(user))
                .creator(user)
                .build());

        List<TaskDTO>taskDTOS = TaskMapper.TASK_MAPPER.tasksToTaskDTOs(tasks);

        TaskDTO task1 = taskDTOS.get(0);

        assertEquals(2, taskDTOS.size());
        assertNotNull(task1);
        assertEquals("some title", task1.getTitle());
        assertEquals("some content", task1.getContent());
        assertEquals(1L, task1.getCreator().getId());
        assertEquals("email", task1.getCreator().getEmail());
        assertEquals("first_name", task1.getCreator().getFirstName());
        assertEquals(UserDTO.class, task1.getCreator().getClass());
        assertNull(task1.getCreator().getLastName());

        TaskDTO task2 = taskDTOS.get(1);

        assertNotNull(task2);
        assertEquals("title2", task2.getTitle());
        assertEquals("content2", task2.getContent());
        assertEquals(1L, task2.getCreator().getId());
        assertEquals("email", task2.getCreator().getEmail());
        assertEquals("first_name", task2.getCreator().getFirstName());
        assertEquals(1, task2.getPerformers().size());
        assertEquals(UserDTO.class, task2.getCreator().getClass());

    }

}
