package com.example.taskmanagementapp.service;

import com.example.taskmanagementapp.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService extends GeneralService<User, Long>{

    User findByEmail(String email);
    Boolean existsByEmail(String email);

    List<User> findUsersByWorkspacesId(Long id);

    List<User>findUsersByTasksId(Long id);

    Boolean existsByEmailAndWorkspacesId(String email, Long workspaceId);
}
