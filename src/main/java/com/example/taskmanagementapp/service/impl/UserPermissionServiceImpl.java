package com.example.taskmanagementapp.service.impl;

import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import com.example.taskmanagementapp.repository.WorkspaceRepository;
import com.example.taskmanagementapp.service.UserPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserPermissionServiceImpl implements UserPermissionService {

    private final JdbcMutableAclService jdbcMutableAclService;
    private final WorkspaceRepository workspaceRepository;

    @Autowired
    public UserPermissionServiceImpl(JdbcMutableAclService jdbcMutableAclService, WorkspaceRepository workspaceRepository) {
        this.jdbcMutableAclService = jdbcMutableAclService;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    public Map<Long, String[]> getUserWorkspacePermissions(User user) {
        Map<Long, String[]> workspacePermissions = new HashMap<>();

        // Get all workspaces for the user
        List<Workspace> userWorkspaces = user.getWorkspaces();

        for (Workspace workspace : userWorkspaces) {
            try {
                ObjectIdentityImpl objectIdentity = new ObjectIdentityImpl(workspace);
                MutableAcl acl = (MutableAcl) jdbcMutableAclService.readAclById(objectIdentity);
                PrincipalSid userSid = new PrincipalSid(user.getEmail());

                // Get all entries for this user
                List<AccessControlEntry> userEntries = acl.getEntries().stream()
                        .filter(entry -> entry.getSid().equals(userSid))
                        .toList();

                // Extract permissions using a Set to avoid duplicates
                Set<String> permissionsSet = new HashSet<>();
                for (AccessControlEntry entry : userEntries) {
                    Permission permission = entry.getPermission();
                    String permissionName = getPermissionName(permission);
                    if (permissionName != null) {
                        permissionsSet.add(permissionName);
                    }
                }

                if (!permissionsSet.isEmpty()) {
                    workspacePermissions.put(workspace.getId(), permissionsSet.toArray(new String[0]));
                }

            } catch (Exception e) {
                // Skip workspaces with no ACL
                continue;
            }
        }

        return workspacePermissions;
    }

    private String getPermissionName(Permission permission) {
        if (permission.equals(BasePermission.READ)) return "READ";
        if (permission.equals(BasePermission.WRITE)) return "WRITE";
        if (permission.equals(BasePermission.CREATE)) return "CREATE";
        if (permission.equals(BasePermission.DELETE)) return "DELETE";
        if (permission.equals(BasePermission.ADMINISTRATION)) return "ADMIN";
        return null;
    }
}
