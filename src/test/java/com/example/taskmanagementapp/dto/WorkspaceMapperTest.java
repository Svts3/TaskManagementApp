package com.example.taskmanagementapp.dto;

import com.example.taskmanagementapp.dto.mappers.WorkspaceMapper;
import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class WorkspaceMapperTest {

    private Workspace workspace;

    private List<Workspace>workspaces;



    @BeforeEach
    void setUp(){
        User user = User
                .builder()
                .id(1L)
                .firstName("firstName")
                .build();
        Task task = Task.builder().title("title").build();

        workspaces = new ArrayList<>();

        workspace = Workspace
                .builder()
                .id(1L)
                .name("workspace")
                .members(List.of(user))
                .tasks(List.of(task))
                .build();
        workspace.setMembers(List.of(user));
    }

    @Test
    void testWorkspaceMapper(){

        WorkspaceDTO workspaceDTO = WorkspaceMapper.WORKSPACE_MAPPER.workspaceToWorkspaceDTO(workspace);

        assertNotNull(workspaceDTO);
        assertNotNull(workspaceDTO.getMembers());
        assertEquals("workspace", workspaceDTO.getName());
        assertEquals(1L, workspaceDTO.getId());
        assertEquals("firstName", workspaceDTO.getMembers().get(0).getFirstName());


    }
    @Test
    void testWorkspaceMapperList(){

        workspaces.add(workspace);
        workspaces.add(Workspace.builder().name("workspace2").build());

        assertEquals(2, workspaces.size());

        List<WorkspaceDTO> workspaceDTOS = WorkspaceMapper.WORKSPACE_MAPPER.workspacesToWorkspaceDTOs(workspaces);

        WorkspaceDTO workspaceDTO1 = workspaceDTOS.get(0);
        assertNotNull(workspaceDTO1);
        assertNotNull(workspaceDTO1.getMembers());
        assertEquals("workspace", workspaceDTO1.getName());
        assertEquals(1L, workspaceDTO1.getId());
        assertEquals("firstName", workspaceDTO1.getMembers().get(0).getFirstName());

        WorkspaceDTO workspaceDTO2 = workspaceDTOS.get(1);
        assertNotNull(workspaceDTO2);
        assertEquals("workspace2", workspaceDTO2.getName());
    }

}
