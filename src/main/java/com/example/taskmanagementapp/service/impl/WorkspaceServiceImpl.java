package com.example.taskmanagementapp.service.impl;

import com.example.taskmanagementapp.dto.mappers.WorkspaceMapper;
import com.example.taskmanagementapp.exception.WorkspaceNotFoundException;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import com.example.taskmanagementapp.repository.WorkspaceRepository;
import com.example.taskmanagementapp.service.UserService;
import com.example.taskmanagementapp.service.WorkspaceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.acls.domain.AccessControlEntryImpl;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
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
    public Workspace save(Workspace entity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        entity.setCreator(userService.findByEmail(authentication.getName()));
        entity.setCreationDate(new Date());
        Workspace savedWorkspace = workspaceRepository.save(entity);
        MutableAcl acl = jdbcMutableAclService.createAcl(new ObjectIdentityImpl(savedWorkspace));

        PrincipalSid sid = new PrincipalSid(authentication);
        acl.insertAce(0, BasePermission.READ, sid, true);
        acl.insertAce(1, BasePermission.CREATE, sid, true);
        acl.insertAce(2, BasePermission.WRITE, sid, true);
        acl.insertAce(3, BasePermission.DELETE, sid, true);
        acl.insertAce(4, BasePermission.ADMINISTRATION, new PrincipalSid(authentication), true);
        jdbcMutableAclService.updateAcl(acl);
        return savedWorkspace;
    }

    @PostFilter("hasPermission(filterObject, 'READ')")
    @Override
    public List<Workspace> findAll() {
        return workspaceRepository.findAll();
    }

    @Override
    public Workspace findById(Long aLong) {
        return workspaceRepository.findById(aLong).orElseThrow(() -> new WorkspaceNotFoundException(String.format("Workspace with %d ID was not found!", aLong)));
    }

    @Transactional
    @Override
    public Workspace update(Workspace entity, Long aLong) {
        Workspace workspace = findById(aLong);
        WorkspaceMapper.WORKSPACE_MAPPER.updateWorkspace(entity, workspace);
        workspace.setLastModifiedDate(new Date());
        return workspaceRepository.save(workspace);
    }

    @Override
    public Workspace deleteById(Long aLong) {
        Workspace workspace = findById(aLong);
        workspaceRepository.deleteById(aLong);
        return workspace;
    }

    @Transactional
    @Override
    public Workspace addUsersToWorkspace(Long workspaceId, List<Long> userIds) {
        Workspace workspace = findById(workspaceId);
        Set<User> users = userIds.stream().map(id -> userService.findById(id)).collect(Collectors.toSet());
        workspace.getMembers().addAll(users);
        MutableAcl acl = (MutableAcl) jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace));
        users.forEach(user -> {
            acl.insertAce(0, BasePermission.READ, new PrincipalSid(user.getEmail()), true);
        });
        jdbcMutableAclService.updateAcl(acl);
        return workspaceRepository.save(workspace);
    }

    @Transactional
    @Override
    public Workspace removeUserFromWorkspace(Long workspaceId, Long userId) {
        Workspace workspace = findById(workspaceId);
        User user = userService.findById(userId);
        workspace.getMembers().remove(user);
        MutableAcl acl = (MutableAcl) jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace));
        acl.getEntries().removeIf(entry -> entry.getSid().equals(new PrincipalSid(user.getEmail())));
        jdbcMutableAclService.updateAcl(acl);
        return workspaceRepository.save(workspace);
    }

    @Override
    public Workspace findByTasksId(Long id) {
        return workspaceRepository.findByTasksId(id);
    }

    @Transactional
    @Override
    public void addPermissionsForUserInWorkspace(Long workspaceId, Long userId, List<String> permissions) {
        Workspace workspace = findById(workspaceId);
        User user = userService.findById(userId);
        MutableAcl acl = (MutableAcl) jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace));

        List<Permission> userPermissions = acl.getEntries().stream().filter(entry -> entry.getSid().equals(new PrincipalSid(user.getEmail()))).map(AccessControlEntry::getPermission).toList();

        Map<Permission, Integer> permissionsToBeInserted = permissions.stream().flatMap(permission -> convertStringToPermission(permission).entrySet().stream()).filter(entry -> !userPermissions.contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        permissionsToBeInserted.forEach((permission, index) -> {
            acl.insertAce(index, permission, new PrincipalSid(user.getEmail()), true);
        });

        jdbcMutableAclService.updateAcl(acl);
    }

    @Transactional
    @Override
    public void removePermissionsForUserInWorkspace(Long workspaceId, Long userId, List<String> permissions) {
        Workspace workspace = findById(workspaceId);

        User user = userService.findById(userId);

        MutableAcl acl = (MutableAcl) jdbcMutableAclService.readAclById(new ObjectIdentityImpl(workspace));

        List<Permission> permissionToDelete = permissions
                .stream()
                .flatMap(permission -> convertStringToPermission(permission).keySet().stream()).toList();

        for (int i = 0; i < acl.getEntries().size(); i++) {
            AccessControlEntry accessControlEntry = acl.getEntries().get(i);
            if (accessControlEntry.getSid().equals(new PrincipalSid(user.getEmail())) &&
                    permissionToDelete.contains(accessControlEntry.getPermission())) {
                acl.deleteAce(i);
            }
        }
        jdbcMutableAclService.updateAcl(acl);
    }

    private Map<Permission, Integer> convertStringToPermission(String permission) {
        return switch (permission) {
            case "READ" -> Map.of(BasePermission.READ, 0);
            case "CREATE" -> Map.of(BasePermission.CREATE, 1);
            case "WRITE" -> Map.of(BasePermission.WRITE, 2);
            case "DELETE" -> Map.of(BasePermission.DELETE, 3);
            case "ADMIN" -> Map.of(BasePermission.ADMINISTRATION, 4);
            default -> throw new IllegalArgumentException("Invalid Permission");
        };
    }
}
