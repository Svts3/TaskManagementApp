package com.example.taskmanagementapp.repository;

import com.example.taskmanagementapp.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Workspace findByTasksId(Long taskId);

}
