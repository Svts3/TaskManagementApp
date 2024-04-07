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
        Workspace savedWorkspace = workspaceRepository.save(entity);
        MutableAcl acl = jdbcMutableAclService.createAcl(new ObjectIdentityImpl(savedWorkspace));

        PrincipalSid sid = new PrincipalSid(authentication);
        acl.insertAce(acl.getEntries().size(), BasePermission.READ, sid, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.CREATE, sid, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.WRITE, sid, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.DELETE, sid, true);
        acl.insertAce(acl.getEntries().size(), BasePermission.ADMINISTRATION, new PrincipalSid(authentication), true);
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
        workspace.getMembers().addAll(users);
        MutableAcl acl = (MutableAcl) jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace));
        users.forEach(user -> {
            acl.insertAce(0, BasePermission.READ, new PrincipalSid(user.getEmail()), true);
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
    public void addPermissionsForUserInWorkspace(@NonNull Long workspaceId, @NonNull Long userId,
                                                 @NonNull List<String> permissions) {
        Workspace workspace = findById(workspaceId);
        User user = userService.findById(userId);
        MutableAcl acl = (MutableAcl) jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace));

        List<Permission> userPermissions = acl
                .getEntries()
                .stream()
                .filter(entry -> entry.getSid().equals(new PrincipalSid(user.getEmail())))
                .map(AccessControlEntry::getPermission).toList();

        List<Permission> permissionsToBeInserted = permissions
                .stream()
                .map(this::convertStringToPermission)
                .filter(permission -> !userPermissions.contains(permission))
                .toList();

        permissionsToBeInserted.forEach(permission -> {
            acl.insertAce(acl.getEntries().size(), permission, new PrincipalSid(user.getEmail()), true);
        });

        jdbcMutableAclService.updateAcl(acl);
    }

    @Transactional
    @Override
    public void removePermissionsForUserInWorkspace(@NonNull Long workspaceId, @NonNull Long userId,
                                                    @NonNull List<String> permissions) {
        Workspace workspace = findById(workspaceId);

        User user = userService.findById(userId);

        MutableAcl acl = (MutableAcl) jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace));

        List<Permission> permissionToDelete = permissions
                .stream()
                .map(this::convertStringToPermission).toList();

        for (int i = 0; i < acl.getEntries().size(); i++) {
            AccessControlEntry accessControlEntry = acl.getEntries().get(i);
            if (accessControlEntry.getSid().equals(new PrincipalSid(user.getEmail())) &&
                    permissionToDelete.contains(accessControlEntry.getPermission())) {
                acl.deleteAce(i);
            }
        }
        jdbcMutableAclService.updateAcl(acl);
    }

    private Permission convertStringToPermission(String permission) {
        return switch (permission.toUpperCase()) {
            case "READ" -> BasePermission.READ;
            case "CREATE" -> BasePermission.CREATE;
            case "WRITE" -> BasePermission.WRITE;
            case "DELETE" -> BasePermission.DELETE;
            case "ADMIN" -> BasePermission.ADMINISTRATION;
            default -> throw new IllegalArgumentException("Invalid Permission");
        };
    }
}
