package com.example.taskmanagementapp.dto.mappers;

import com.example.taskmanagementapp.dto.WorkspaceDTO;
import com.example.taskmanagementapp.model.Workspace;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(builder = @Builder(disableBuilder = true))
public interface WorkspaceMapper {

    WorkspaceMapper WORKSPACE_MAPPER = Mappers.getMapper(WorkspaceMapper.class);

    WorkspaceDTO workspaceToWorkspaceDTO(Workspace workspace);

    List<WorkspaceDTO>workspacesToWorkspaceDTOs(List<Workspace> workspaces);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateWorkspace(Workspace updatedWorkspace, @MappingTarget Workspace workspace);

}
