package com.example.taskmanagementapp.service;

import com.example.taskmanagementapp.model.User;
import java.util.Map;

public interface UserPermissionService {

    /**
     * Get all permissions for a user across all workspaces
     * @param user The user to get permissions for
     * @return Map of workspace IDs to list of permission strings
     */
    Map<Long, String[]> getUserWorkspacePermissions(User user);
}
