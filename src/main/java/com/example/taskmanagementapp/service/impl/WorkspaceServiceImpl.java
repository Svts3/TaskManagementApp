package com.example.taskmanagementapp.service.impl;

import com.example.taskmanagementapp.dto.mappers.WorkspaceMapper;
import com.example.taskmanagementapp.exception.UserNotInWorkspaceException;
import com.example.taskmanagementapp.exception.WorkspaceNotFoundException;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import com.example.taskmanagementapp.repository.WorkspaceRepository;
import com.example.taskmanagementapp.service.UserService;
import com.example.taskmanagementapp.service.WorkspaceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PostFilter;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    private WorkspaceRepository workspaceRepository;

    private JdbcMutableAclService jdbcMutableAclService;

    private UserService userService;


    @Autowired
    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepository, JdbcMutableAclService jdbcMutableAclService, UserService userService) {
        this.workspaceRepository = workspaceRepository;
        this.jdbcMutableAclService = jdbcMutableAclService;
        this.userService = userService;
    }

    @Transactional
    @Override
    public Workspace save(@NonNull Workspace entity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User)authentication.getPrincipal();
        entity.setMembers(List.of(user));
        Workspace savedWorkspace = workspaceRepository.save(entity);
        MutableAcl acl = jdbcMutableAclService.createAcl(new ObjectIdentityImpl(savedWorkspace));

        // Create principal SID based on user email (username)
        PrincipalSid sid = new PrincipalSid(user.getEmail());
        acl.insertAce(acl.getEntries().size(), BasePermission.READ, sid, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.CREATE, sid, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.WRITE, sid, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.DELETE, sid, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.ADMINISTRATION, sid, true);
        jdbcMutableAclService.updateAcl(acl);
        return savedWorkspace;
    }

    @PostFilter("hasPermission(filterObject, 'READ')")
    public List<Workspace> findAll() {
        return workspaceRepository.findAll();
    }

    @Override
    public Workspace findById(@NonNull Long aLong) {
        return workspaceRepository.findById(aLong).orElseThrow(() -> new WorkspaceNotFoundException(String.format("Workspace with %d ID was not found!", aLong)));
    }

    @Transactional
    @Override
    public Workspace update(@NonNull Workspace entity, @NonNull Long aLong) {
        Workspace workspace = findById(aLong);
        WorkspaceMapper.WORKSPACE_MAPPER.updateWorkspace(entity, workspace);
        return workspaceRepository.save(workspace);
    }

    @Override
    public Workspace deleteById(@NonNull Long aLong) {
        Workspace workspace = findById(aLong);
        workspaceRepository.deleteById(aLong);
        return workspace;
    }

    @Transactional
    @Override
    public Workspace addUsersToWorkspace(@NonNull Long workspaceId, @NonNull List<Long> userIds) {
        Workspace workspace = findById(workspaceId);
        Set<User> users = userIds.stream().map(id -> userService.findById(id)).collect(Collectors.toSet());

        // Filter out users who are already members
        Set<User> newUsers = users.stream()
                .filter(user -> !workspace.getMembers().contains(user))
                .collect(Collectors.toSet());

        if (newUsers.isEmpty()) {
            return workspace; // No new users to add
        }

        // Add all new users to the workspace
        workspace.getMembers().addAll(newUsers);

        // Read and update ACL
        MutableAcl acl = (MutableAcl) jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace));

        // Add READ permission for each new user
        newUsers.forEach(user -> {
            PrincipalSid sid = new PrincipalSid(user.getEmail());

            // Check if user already has READ permission
            boolean hasReadPermission = acl.getEntries().stream()
                    .anyMatch(ace -> ace.getSid().equals(sid) && 
                                    ace.getPermission().equals(BasePermission.READ));

            // Add READ permission if not already present
            if (!hasReadPermission) {
                acl.insertAce(acl.getEntries().size(), BasePermission.READ, sid, true);
            }

            // Update bidirectional relationship
            user.getWorkspaces().add(workspace);
            userService.save(user);
        });

        jdbcMutableAclService.updateAcl(acl);
        return workspaceRepository.save(workspace);
    }

    @Transactional
    @Override
    public Workspace removeUserFromWorkspace(@NonNull Long workspaceId, @NonNull Long userId) {
        Workspace workspace = findById(workspaceId);
        User user = userService.findById(userId);
        if (!workspace.getMembers().contains(user)) {
            throw new UserNotInWorkspaceException(String.format("User %d is not in the workspace", userId));
        }
        workspace.getMembers().removeIf(user1 -> user1.getId().equals(userId));
        MutableAcl acl = (MutableAcl) jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace));
        acl.getEntries().removeIf(entry -> entry.getSid().equals(new PrincipalSid(user.getEmail())));
        jdbcMutableAclService.updateAcl(acl);
        return workspaceRepository.save(workspace);
    }

    @Override
    public Workspace findByTasksId(@NonNull Long id) {
        return workspaceRepository.findByTasksId(id).orElseThrow(
                () -> new WorkspaceNotFoundException(String.format("Workspace by task id %d was not found!", id)));
    }

    @Transactional
    @Override
    public Workspace addUsersToWorkspaceByEmails(@NonNull Long workspaceId, @NonNull List<String> emails) {
        Workspace workspace = findById(workspaceId);

        // Find users by their emails
        Set<User> users = emails.stream()
            .map(email -> userService.findByEmail(email))
            .collect(Collectors.toSet());

        // Filter out users who are already members
        Set<User> newUsers = users.stream()
                .filter(user -> !workspace.getMembers().contains(user))
                .collect(Collectors.toSet());

        if (newUsers.isEmpty()) {
            return workspace; // No new users to add
        }

        // Add all new users to the workspace
        workspace.getMembers().addAll(newUsers);

        // Read and update ACL
        MutableAcl acl = (MutableAcl) jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace));

        // Add READ permission for each new user
        newUsers.forEach(user -> {
            PrincipalSid sid = new PrincipalSid(user.getEmail());

            // Check if user already has READ permission
            boolean hasReadPermission = acl.getEntries().stream()
                    .anyMatch(ace -> ace.getSid().equals(sid) && 
                                    ace.getPermission().equals(BasePermission.READ));

            // Add READ permission if not already present
            if (!hasReadPermission) {
                acl.insertAce(acl.getEntries().size(), BasePermission.READ, sid, true);
            }

            // Update bidirectional relationship
            user.getWorkspaces().add(workspace);
            userService.save(user);
        });

        jdbcMutableAclService.updateAcl(acl);
        return workspaceRepository.save(workspace);
    }

    @Transactional
    @Override
    public void addPermissionsForUserInWorkspace(@NonNull Long workspaceId, @NonNull Long userId,
                                                 @NonNull List<String> permissions) {
        Workspace workspace = findById(workspaceId);
        User user = userService.findById(userId);

        // Check if user is a member of the workspace
        if (!workspace.getMembers().contains(user)) {
            throw new UserNotInWorkspaceException(String.format("User with ID %d is not a member of workspace with ID %d", 
                userId, workspaceId));
        }

        ObjectIdentityImpl objectIdentity = new ObjectIdentityImpl(workspace);
        MutableAcl acl;
        try {
            acl = (MutableAcl) jdbcMutableAclService.readAclById(objectIdentity);
        } catch (Exception e) {
            // If ACL doesn't exist, create it
            acl = jdbcMutableAclService.createAcl(objectIdentity);
        }

        PrincipalSid userSid = new PrincipalSid(user.getEmail());

        // Get existing permissions for this user
        List<Permission> userPermissions = acl.getEntries().stream()
                .filter(entry -> entry.getSid().equals(userSid))
                .map(AccessControlEntry::getPermission)
                .toList();

        // Process each permission to add
        for (String permString : permissions) {
            Permission permission = convertStringToPermission(permString);

            // Check if this permission is already assigned to this user
            boolean hasPermission = userPermissions.contains(permission);

            if (!hasPermission) {
                // Add the new permission
                acl.insertAce(acl.getEntries().size(), permission, userSid, true);
            }
        }

        // Save changes to the ACL
        jdbcMutableAclService.updateAcl(acl);
    }

    @Transactional
    @Override
    public void removePermissionsForUserInWorkspace(@NonNull Long workspaceId, @NonNull Long userId,
                                                    @NonNull List<String> permissions) {
        Workspace workspace = findById(workspaceId);
        User user = userService.findById(userId);

        // Check if user is a member of the workspace
        if (!workspace.getMembers().contains(user)) {
            throw new UserNotInWorkspaceException(String.format("User with ID %d is not a member of workspace with ID %d", 
                userId, workspaceId));
        }

        MutableAcl acl = (MutableAcl) jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace));
        PrincipalSid userSid = new PrincipalSid(user.getEmail());

        // Convert permissions to delete
        List<Permission> permissionsToDelete = permissions
                .stream()
                .map(this::convertStringToPermission)
                .toList();

        // Create a list of ACE indices to delete (in reverse order to avoid index shifting)
        List<Integer> indicesToDelete = acl.getEntries().stream()
                .filter(ace -> ace.getSid().equals(userSid) && permissionsToDelete.contains(ace.getPermission()))
                .map(ace -> acl.getEntries().indexOf(ace))
                .sorted((a, b) -> Integer.compare(b, a)) // Sort in reverse order
                .toList();

        // Delete the ACEs
        for (Integer index : indicesToDelete) {
            acl.deleteAce(index);
        }

        // Only update if changes were made
        if (!indicesToDelete.isEmpty()) {
            jdbcMutableAclService.updateAcl(acl);
        }
    }

    private Permission convertStringToPermission(String permission) {
        return switch (permission.toUpperCase()) {
            case "READ" -> BasePermission.READ;
            case "CREATE" -> BasePermission.CREATE;
            case "WRITE" -> BasePermission.WRITE;
            case "DELETE" -> BasePermission.DELETE;
            case "ADMIN", "ADMINISTRATOR", "ADMINISTRATION" -> BasePermission.ADMINISTRATION;
            default -> throw new IllegalArgumentException("Invalid Permission: " + permission);
        };
    }
}
