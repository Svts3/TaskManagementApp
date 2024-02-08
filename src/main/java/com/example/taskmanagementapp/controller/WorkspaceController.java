package com.example.taskmanagementapp.controller;

import com.example.taskmanagementapp.dto.WorkspaceDTO;
import com.example.taskmanagementapp.dto.mappers.WorkspaceMapper;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import com.example.taskmanagementapp.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workspaces")
public class WorkspaceController {


    public WorkspaceService workspaceService;

    private JdbcMutableAclService jdbcMutableAclService;

    @Autowired
    public WorkspaceController(WorkspaceService workspaceService, JdbcMutableAclService jdbcMutableAclService) {
        this.workspaceService = workspaceService;
        this.jdbcMutableAclService = jdbcMutableAclService;
    }

    @GetMapping("/")
    public ResponseEntity<List<WorkspaceDTO>> findAll() {
        List<Workspace> workspaces = workspaceService.findAll();
        List<WorkspaceDTO> workspaceDTOS = WorkspaceMapper.WORKSPACE_MAPPER.workspacesToWorkspaceDTOs(workspaces);
        return ResponseEntity.ok(workspaceDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceDTO> findById(@PathVariable(name = "id") Long id) {
        Workspace workspace = workspaceService.findById(id);
        WorkspaceDTO workspaceDTO = WorkspaceMapper.WORKSPACE_MAPPER.workspaceToWorkspaceDTO(workspace);
        return ResponseEntity.ok(workspaceDTO);
    }

    @PostMapping("/")
    public ResponseEntity<WorkspaceDTO> save(@RequestBody Workspace workspace) {
        Workspace workspace1 = workspaceService.save(workspace);
        WorkspaceDTO workspaceDTO = WorkspaceMapper.WORKSPACE_MAPPER.workspaceToWorkspaceDTO(workspace1);
        return ResponseEntity.ok(workspaceDTO);
    }

    @PreAuthorize("hasPermission(#id, 'com.example.taskmanagementapp.model.Workspace', 'ADMINISTRATION')")
    @PostMapping("/{id}/users")
    public ResponseEntity<WorkspaceDTO> addUsersToWorkspace(@PathVariable("id") Long id, @RequestBody List<Long> userIds) {
        Workspace workspace = workspaceService.addUsersToWorkspace(id, userIds);
        WorkspaceDTO workspaceDTO = WorkspaceMapper.WORKSPACE_MAPPER.workspaceToWorkspaceDTO(workspace);
        return ResponseEntity.ok(workspaceDTO);
    }

    @PreAuthorize("hasPermission(#id, 'com.example.taskmanagementapp.model.Workspace', 'UPDATE')")
    @PatchMapping("/{id}")
    public ResponseEntity<WorkspaceDTO> update(@PathVariable("id") Long id, @RequestBody Workspace workspace) {
        Workspace workspace1 = workspaceService.update(workspace, id);
        WorkspaceDTO workspaceDTO = WorkspaceMapper.WORKSPACE_MAPPER.workspaceToWorkspaceDTO(workspace1);
        return ResponseEntity.ok(workspaceDTO);
    }

    @PreAuthorize("hasPermission(#id, 'com.example.taskmanagementapp.model.Workspace', 'ADMINISTRATION')")
    @DeleteMapping("/{id}/users/{userId}")
    public ResponseEntity<WorkspaceDTO> removeUserFromWorkspace(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        Workspace workspace = workspaceService.removeUserFromWorkspace(id, userId);
        WorkspaceDTO workspaceDTO = WorkspaceMapper.WORKSPACE_MAPPER.workspaceToWorkspaceDTO(workspace);
        return ResponseEntity.ok(workspaceDTO);
    }

    @PreAuthorize("hasPermission(#id, 'com.example.taskmanagementapp.model.Workspace', 'ADMINISTRATION')")
    @DeleteMapping("/{id}")
    public ResponseEntity<WorkspaceDTO> deleteById(@PathVariable(name = "id") Long id) {
        Workspace workspace = workspaceService.deleteById(id);
        WorkspaceDTO workspaceDTO = WorkspaceMapper.WORKSPACE_MAPPER.workspaceToWorkspaceDTO(workspace);
        return ResponseEntity.ok(workspaceDTO);
    }

    @PreAuthorize("hasPermission(#workspaceId, 'com.example.taskmanagementapp.model.Workspace', 'ADMINISTRATION')")
    @PostMapping("/{workspaceId}/users/{userId}/permissions")
    public ResponseEntity<String> addPermissionsForUserInWorkspace(@PathVariable(name = "workspaceId") Long workspaceId,
                                                                   @PathVariable(name = "userId") Long userId,
                                                                   @RequestBody List<String> permissions) {
        workspaceService.addPermissionsForUserInWorkspace(workspaceId, userId, permissions);
        return ResponseEntity.ok("Permissions was successfully granted!");
    }
    @PreAuthorize("hasPermission(#workspaceId, 'com.example.taskmanagementapp.model.Workspace', 'ADMINISTRATION')")
    @DeleteMapping("/{workspaceId}/users/{userId}/permissions")
    public ResponseEntity<String> removePermissionsForUserInWorkspace(@PathVariable(name = "workspaceId") Long workspaceId,
                                                                   @PathVariable(name = "userId") Long userId,
                                                                   @RequestBody List<String> permissions) {
        workspaceService.removePermissionsForUserInWorkspace(workspaceId, userId, permissions);
        return ResponseEntity.ok("Permissions was successfully removed!");
    }


}
