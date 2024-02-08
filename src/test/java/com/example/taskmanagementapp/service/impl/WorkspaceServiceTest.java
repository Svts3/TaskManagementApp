package com.example.taskmanagementapp.service.impl;

import com.example.taskmanagementapp.exception.WorkspaceNotFoundException;
import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import com.example.taskmanagementapp.repository.WorkspaceRepository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private WorkspaceServiceImpl workspaceService;

    private Workspace workspace;

    private static final String workspaceNotFoundExceptionMessage = "Workspace with %d ID was not found!";

    @BeforeEach
    void setUp() {
        List<User> members = new ArrayList<>();
        members.add(User.builder().id(2L).build());
        workspace = Workspace.builder().id(1L).name("workspace").tasks(List.of(new Task())).creator(new User()).members(members).build();

    }

    @Test
    void testSave() {
        when(workspaceRepository.save(workspace)).thenReturn(workspace);
        Workspace savedWorkspace = workspaceService.save(workspace);

        assertNotNull(savedWorkspace);
        assertNotNull(savedWorkspace.getCreator());
        assertNotNull(savedWorkspace.getCreationDate());

        assertEquals(1L, savedWorkspace.getId());
        assertEquals("workspace", savedWorkspace.getName());
        assertEquals(1, savedWorkspace.getTasks().size());
        assertEquals(1, savedWorkspace.getMembers().size());
        verify(workspaceRepository, times(1)).save(workspace);
    }

    @Test
    void findById() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        Workspace foundWorkspace = workspaceService.findById(1L);
        assertNotNull(foundWorkspace);
        assertSame(workspace, foundWorkspace);
        verify(workspaceRepository, times(1)).findById(1L);
    }

    @Test
    void findById_NotFound_ThrowWorkspaceNotFoundException() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(WorkspaceNotFoundException.class, () -> workspaceService.findById(1L));
        assertEquals(String.format(workspaceNotFoundExceptionMessage, 1L), exception.getMessage());
    }

    @Test
    void testFindAll() {
        Workspace workspace2 = new Workspace();
        when(workspaceRepository.findAll()).thenReturn(List.of(workspace, workspace2));
        List<Workspace> foundWorkspaces = workspaceService.findAll();
        assertNotNull(foundWorkspaces);
        assertEquals(2, foundWorkspaces.size());
        verify(workspaceRepository, times(1)).findAll();
    }

    @Test
    void testUpdate() {
        Workspace workspace2 = Workspace.builder().name("workspaceUpd").build();
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(workspace)).thenReturn(workspace);
        Workspace updatedWorkspace = workspaceService.update(workspace2, 1L);

        assertNotNull(updatedWorkspace);
        assertNotNull(updatedWorkspace.getLastModifiedDate());
        assertEquals(1L, updatedWorkspace.getId());
        assertEquals("workspaceUpd", updatedWorkspace.getName());
        assertEquals(1, updatedWorkspace.getTasks().size());
        assertEquals(1, updatedWorkspace.getMembers().size());
        verify(workspaceRepository, times(1)).findById(1L);
        verify(workspaceRepository, times(1)).save(workspace);
    }

    @Test
    void testUpdate_NotFound_ThrowWorkspaceNotFoundException() {
        Workspace workspace2 = Workspace.builder().name("workspaceUpd").build();
        when(workspaceRepository.findById(1L)).thenReturn(Optional.empty());
        when(workspaceRepository.save(workspace)).thenReturn(workspace);
        RuntimeException exception = assertThrows(WorkspaceNotFoundException.class, () -> workspaceService.update(workspace2, 1L));
        assertEquals(String.format(workspaceNotFoundExceptionMessage, 1L), exception.getMessage());
    }

    @Test
    void testDeleteById() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        Workspace deletedWorkspace = workspaceService.deleteById(1L);
        assertNotNull(deletedWorkspace);
        assertSame(workspace, deletedWorkspace);
        verify(workspaceRepository, times(1)).findById(1L);
        verify(workspaceRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteById_NotFound_ThrowWorkspaceNotFoundException() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(WorkspaceNotFoundException.class, () -> workspaceService.deleteById(1L));
        assertEquals(String.format(workspaceNotFoundExceptionMessage, 1L), exception.getMessage());
        verify(workspaceRepository, times(1)).findById(1L);
    }

    @Test
    void testAddUsersToWorkspace() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(workspace)).thenReturn(workspace);

        Workspace updatedWorkspace = workspaceService.addUsersToWorkspace(1L, List.of(1L));
        assertNotNull(updatedWorkspace);
        assertEquals(2, updatedWorkspace.getMembers().size());
        verify(workspaceRepository, times(1)).findById(1L);
        verify(workspaceRepository, times(1)).save(workspace);
    }

    @Test
    void testAddUsersToWorkspace_NotFound_ThrowWorkspaceNotFoundException() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.empty());
        when(workspaceRepository.save(workspace)).thenReturn(workspace);

        RuntimeException exception = assertThrows(WorkspaceNotFoundException.class,
                () -> workspaceService.addUsersToWorkspace(1L, List.of(1L)));
        assertEquals(String.format(workspaceNotFoundExceptionMessage, 1L), exception.getMessage());
        verify(workspaceRepository, times(1)).findById(1L);
    }

    @Test
    void testRemoveUsersFromWorkspace() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(workspace)).thenReturn(workspace);

        Workspace updatedWorkspace = workspaceService.removeUserFromWorkspace(1L, 2L);
        assertNotNull(updatedWorkspace);
        assertEquals(0, updatedWorkspace.getMembers().size());
        verify(workspaceRepository, times(1)).findById(1L);
        verify(workspaceRepository, times(1)).save(workspace);
    }


}
