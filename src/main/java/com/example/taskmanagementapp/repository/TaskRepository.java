package com.example.taskmanagementapp.repository;

import com.example.taskmanagementapp.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByWorkspaceId(Long id);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.performers WHERE t.id = :id")
    Optional<Task> findByIdWithPerformers(@Param("id") Long id);

}
