package com.example.taskmanagementapp.controller;

import com.example.taskmanagementapp.dto.TaskDTO;
import com.example.taskmanagementapp.dto.mappers.TaskMapper;
import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.service.TaskService;
import com.example.taskmanagementapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private TaskService taskService;

    private UserService userServiceImpl;

    @Autowired
    public TaskController(TaskService taskService, UserService userServiceImpl) {
        this.taskService = taskService;
        this.userServiceImpl = userServiceImpl;
    }

    @GetMapping("/")
    public ResponseEntity<List<TaskDTO>> findAll() {
        List<Task> tasks = taskService.findAll();
        List<TaskDTO> taskDTOS = TaskMapper.TASK_MAPPER.tasksToTaskDTOs(tasks);
        return ResponseEntity.ok(taskDTOS);
    }
    @PreAuthorize("hasPermission(#id, 'com.example.taskmanagementapp.model.Workspace', 'READ')")
    @GetMapping("/workspace/{id}")
    public ResponseEntity<List<TaskDTO>> findByWorkspaceId(@PathVariable(name = "id") Long id) {
        List<Task> tasks = taskService.findByWorkspaceId(id);
        List<TaskDTO> taskDTOS = TaskMapper.TASK_MAPPER.tasksToTaskDTOs(tasks);
        return ResponseEntity.ok(taskDTOS);
    }
    @PreAuthorize("hasPermission(workspaceServiceImpl.findByTasksId(#id), 'com.example.taskmanagementapp.model.Workspace', 'READ')")
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> findById(@PathVariable(name = "id") Long id) {
        Task task = taskService.findById(id);
        TaskDTO taskDTO = TaskMapper.TASK_MAPPER.taskToTaskDTO(task);
        return ResponseEntity.ok(taskDTO);
    }
    @PreAuthorize("hasPermission(workspaceServiceImpl.findByTasksId(#id), 'UPDATE')")
    @PatchMapping("/{id}")
    public ResponseEntity<TaskDTO> update(@PathVariable(name = "id") Long id, @RequestBody Task task) {
        Task task1 = taskService.update(task, id);
        TaskDTO taskDTO = TaskMapper.TASK_MAPPER.taskToTaskDTO(task1);
        return ResponseEntity.ok(taskDTO);
    }
    @PreAuthorize("hasPermission(#task.workspace, 'CREATE')")
    @PostMapping("/")
    public ResponseEntity<TaskDTO> save(@RequestBody Task task) {
        Task task1 = taskService.save(task);
        TaskDTO taskDTO = TaskMapper.TASK_MAPPER.taskToTaskDTO(task1);
        return ResponseEntity.ok(taskDTO);
    }
    @PreAuthorize("hasPermission(workspaceServiceImpl.findByTasksId(#id), 'UPDATE')")
    @PostMapping("/{id}/users")
    public ResponseEntity<TaskDTO> addPerformersToTask(@PathVariable("id") Long id, @RequestBody List<Long> userIds) {
        Task task = taskService.addPerformersToTask(id, userIds);
        TaskDTO taskDTO = TaskMapper.TASK_MAPPER.taskToTaskDTO(task);
        return ResponseEntity.ok(taskDTO);
    }
    @PreAuthorize("hasPermission(workspaceServiceImpl.findByTasksId(#id), 'UPDATE')")
    @DeleteMapping("/{id}/users/{userId}")
    public ResponseEntity<TaskDTO> removePerformerFromTask(@PathVariable("id") Long id, @PathVariable("userId") Long performerId) {
        Task task = taskService.removePerformerFromTask(id, performerId);
        TaskDTO taskDTO = TaskMapper.TASK_MAPPER.taskToTaskDTO(task);
        return ResponseEntity.ok(taskDTO);
    }
    @PreAuthorize("hasPermission(workspaceServiceImpl.findByTasksId(#id), 'DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<TaskDTO> deleteById(@PathVariable(name = "id") Long id) {
        Task task1 = taskService.deleteById(id);
        TaskDTO taskDTO = TaskMapper.TASK_MAPPER.taskToTaskDTO(task1);
        return ResponseEntity.ok(taskDTO);
    }


}
