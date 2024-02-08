package com.example.taskmanagementapp.service.impl;

import com.example.taskmanagementapp.dto.mappers.TaskMapper;
import com.example.taskmanagementapp.exception.TaskNotFoundException;
import com.example.taskmanagementapp.exception.UserNotFoundException;
import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.repository.TaskRepository;
import com.example.taskmanagementapp.repository.UserRepository;
import com.example.taskmanagementapp.service.TaskService;
import com.example.taskmanagementapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private TaskRepository taskRepository;

    private UserService userService;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    @Override
    public Task save(Task entity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        entity.setCreationDate(new Date());
        entity.setCreator(userService.findByEmail(authentication.getName()));
        return taskRepository.save(entity);
    }

    @Override
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Override
    public Task findById(Long aLong) {
        return taskRepository.findById(aLong).orElseThrow(
                () -> new TaskNotFoundException(String.format("Task with %d ID was not found!", aLong)));
    }

    @Override
    public Task update(Task entity, Long aLong) {
        Task task = findById(aLong);
        TaskMapper.TASK_MAPPER.updateTask(entity, task);
        task.setLastModifiedDate(new Date());
        return taskRepository.save(task);
    }

    @Override
    public Task deleteById(Long aLong) {
        Task task = findById(aLong);
        taskRepository.deleteById(aLong);
        return task;
    }

    @Override
    public List<Task> findByWorkspaceId(Long id) {
        return taskRepository.findByWorkspaceId(id);
    }

    @Override
    public Task addPerformersToTask(Long taskId, List<Long> userIds) {
        Task task = findById(taskId);

        List<User> performersToAdd = userIds.stream().map(userId -> userService.findById(userId)).toList();

        if (!performersToAdd.stream().allMatch(user -> user.getWorkspaces().contains(task.getWorkspace()))) {
            throw new RuntimeException("Users are not in workspace!");
        }
        task.getPerformers().addAll(performersToAdd);
        return taskRepository.save(task);
    }

    @Override
    public Task removePerformerFromTask(Long taskId, Long performerId) {
        Task task = findById(taskId);
        task.getPerformers().removeIf(performer -> performer.getId().equals(performerId));
        return taskRepository.save(task);
    }
}
