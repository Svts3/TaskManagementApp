package com.example.taskmanagementapp.service.impl;

import com.example.taskmanagementapp.dto.mappers.TaskMapper;
import com.example.taskmanagementapp.exception.TaskNotFoundException;
import com.example.taskmanagementapp.exception.UserNotInWorkspaceException;
import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import com.example.taskmanagementapp.repository.TaskRepository;
import com.example.taskmanagementapp.service.TaskService;
import com.example.taskmanagementapp.service.UserService;
import com.example.taskmanagementapp.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private TaskRepository taskRepository;

    private UserService userService;

    private WorkspaceService workspaceService;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository, UserService userService,
                           WorkspaceService workspaceService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.workspaceService = workspaceService;
    }

    @Override
    public Task save(@NonNull Task entity) {
        return taskRepository.save(entity);
    }

    @PostFilter("filterObject.creator.id==authentication.principal.id")
    @Override
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Override
    public Task findById(@NonNull Long aLong) {
        return taskRepository.findById(aLong).orElseThrow(
                () -> new TaskNotFoundException(String.format("Task with %d ID was not found!", aLong)));
    }

    @Override
    public Task update(@NonNull Task entity, @NonNull Long aLong) {
        Task task = findById(aLong);
        TaskMapper.TASK_MAPPER.updateTask(entity, task);
        return taskRepository.save(task);
    }

    @Override
    public Task deleteById(@NonNull Long aLong) {
        Task task = findById(aLong);
        taskRepository.deleteById(aLong);
        return task;
    }

    @Override
    public List<Task> findByWorkspaceId(@NonNull Long id) {
        return taskRepository.findByWorkspaceId(id);
    }

    @Override
    public Task addPerformersToTask(@NonNull Long taskId, @NonNull List<Long> userIds) {
        Task task = findById(taskId);

        List<User> performersToAdd = userIds.stream().map(userId -> userService.findById(userId)).toList();

        for (User performer : performersToAdd) {
            List<Workspace> performerWorkspaces = performer.getWorkspaces();
                if (!performerWorkspaces.contains(task.getWorkspace())) {
                    throw new UserNotInWorkspaceException(String.format("User[%s] not in workspace!", performer.getEmail()));
                }
        }

        task.getPerformers().addAll(performersToAdd);
        return taskRepository.save(task);
    }

    @Override
    public Task removePerformerFromTask(@NonNull Long taskId, @NonNull Long performerId) {
        Task task = findById(taskId);
        User performer = userService.findById(performerId);
        if(!task.getPerformers().removeIf(p -> p.getId().equals(performer.getId()))){
            throw new UserNotInWorkspaceException(String.format("User[%s] is not in workspace!",
                    performer.getEmail()));
        };
        return taskRepository.save(task);
    }
}
