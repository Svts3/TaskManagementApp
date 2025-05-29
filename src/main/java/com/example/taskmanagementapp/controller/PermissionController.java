package com.example.taskmanagementapp.controller;

import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.service.UserPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/permissions")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PermissionController {

    private final UserPermissionService userPermissionService;

    @Autowired
    public PermissionController(UserPermissionService userPermissionService) {
        this.userPermissionService = userPermissionService;
    }

    /**
     * Get the permissions for the currently authenticated user
     * @return Map of workspace IDs to permission strings
     */
    @GetMapping("/current")
    public ResponseEntity<Map<Long, String[]>> getCurrentUserPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Map<Long, String[]> permissions = userPermissionService.getUserWorkspacePermissions(currentUser);
        return ResponseEntity.ok(permissions);
    }

    /**
     * Get the permissions for a specific workspace for the currently authenticated user
     * @param workspaceId The ID of the workspace
     * @return Array of permission strings
     */
    @GetMapping("/workspaces/{workspaceId}")
    public ResponseEntity<String[]> getUserPermissionsForWorkspace(
            @PathVariable("workspaceId") Long workspaceId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Map<Long, String[]> allPermissions = userPermissionService.getUserWorkspacePermissions(currentUser);
        String[] workspacePermissions = allPermissions.getOrDefault(workspaceId, new String[0]);

        return ResponseEntity.ok(workspacePermissions);
    }
}
