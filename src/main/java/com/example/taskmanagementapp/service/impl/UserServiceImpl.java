package com.example.taskmanagementapp.service.impl;

import com.example.taskmanagementapp.dto.mappers.UserMapper;
import com.example.taskmanagementapp.exception.TaskNotFoundException;
import com.example.taskmanagementapp.exception.UserNotFoundException;
import com.example.taskmanagementapp.exception.WorkspaceNotFoundException;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.repository.TaskRepository;
import com.example.taskmanagementapp.repository.UserRepository;
import com.example.taskmanagementapp.repository.WorkspaceRepository;
import com.example.taskmanagementapp.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    private WorkspaceRepository workspaceRepository;

    private TaskRepository taskRepository;


    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           WorkspaceRepository workspaceRepository,
                           TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public User save(@NonNull User entity) {
        return userRepository.save(entity);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(@NonNull Long aLong) {
        return userRepository.findById(aLong).orElseThrow(
                () -> new UserNotFoundException(String.format("User with %d ID was not found!", aLong)));
    }

    @Transactional
    @Override
    public User update(@NonNull User entity, @NonNull Long aLong) {
        User user = findById(aLong);
        UserMapper.USER_MAPPER.updateUser(entity, user);
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public User deleteById(@NonNull Long aLong) {
        User user = findById(aLong);
        userRepository.deleteById(aLong);
        return user;
    }

    @Override
    public User findByEmail(@NonNull String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException(String.format("User with %s email was not found!", email)));
    }

    @Override
    public Boolean existsByEmail(@NonNull String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<User> findUsersByWorkspacesId(@NonNull Long id) {
        if (!workspaceRepository.existsById(id)) {
            throw new WorkspaceNotFoundException(String.format("Workspace with ID %d was not found!", id));
        }
        return userRepository.findUsersByWorkspacesId(id);
    }

    @Override
    public List<User> findUsersByTasksId(@NonNull Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(String.format("Task with ID %d was not found!", id));
        }
        return userRepository.findUsersByTasksId(id);
    }

    @Override
    public Boolean existsByEmailAndWorkspacesId(@NonNull String email, @NonNull Long workspaceId) {
        return userRepository.existsByEmailAndWorkspacesId(email, workspaceId);
    }
}
