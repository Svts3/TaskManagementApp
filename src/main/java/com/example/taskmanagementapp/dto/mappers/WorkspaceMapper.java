package com.example.taskmanagementapp.dto.mappers;

import com.example.taskmanagementapp.dto.WorkspaceDTO;
import com.example.taskmanagementapp.model.Workspace;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper(builder = @Builder(disableBuilder = true))
public interface WorkspaceMapper {

    WorkspaceMapper WORKSPACE_MAPPER = Mappers.getMapper(WorkspaceMapper.class);

    WorkspaceDTO workspaceToWorkspaceDTO(Workspace workspace);

    List<WorkspaceDTO>workspacesToWorkspaceDTOs(List<Workspace> workspaces);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateWorkspace(Workspace updatedWorkspace, @MappingTarget Workspace workspace);

    /**
     * Safely handles updating collections in a workspace.
     * Use this method to manually implement collection handling in the generated implementation class.
     */
    default void updateWorkspaceCollections(Workspace updatedWorkspace, Workspace workspace) {
        if (updatedWorkspace == null) {
            return;
        }

        // Handle members collection
        if (updatedWorkspace.getMembers() != null) {
            if (workspace.getMembers() != null) {
                // Create a new ArrayList to ensure mutability
                workspace.setMembers(new ArrayList<>(workspace.getMembers()));
                workspace.getMembers().clear();
                workspace.getMembers().addAll(updatedWorkspace.getMembers());
            } else {
                workspace.setMembers(new ArrayList<>(updatedWorkspace.getMembers()));
            }
        }

        // Handle tasks collection
        if (updatedWorkspace.getTasks() != null) {
            if (workspace.getTasks() != null) {
                // Create a new ArrayList to ensure mutability
                workspace.setTasks(new ArrayList<>(workspace.getTasks()));
                workspace.getTasks().clear();
                workspace.getTasks().addAll(updatedWorkspace.getTasks());
            } else {
                workspace.setTasks(new ArrayList<>(updatedWorkspace.getTasks()));
            }
        }
    }

}
