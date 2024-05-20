package com.example.taskmanagementapp.repository;

import com.example.taskmanagementapp.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Optional<Workspace> findByTasksId(Long taskId);
}
