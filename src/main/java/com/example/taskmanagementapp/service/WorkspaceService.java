package com.example.taskmanagementapp.service;

import com.example.taskmanagementapp.model.Workspace;

import java.util.List;
import java.util.Optional;

public interface WorkspaceService extends GeneralService<Workspace, Long>{
    Workspace addUsersToWorkspace(Long workspaceId, List<Long>userIds);

    Workspace removeUserFromWorkspace(Long workspaceId, Long userId);

    void addPermissionsForUserInWorkspace(Long workspaceId, Long userId, List<String> permissions);

    void removePermissionsForUserInWorkspace(Long workspaceId, Long userId, List<String>permissions);

    Workspace findByTasksId(Long id);
}
