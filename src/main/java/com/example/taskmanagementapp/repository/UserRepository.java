package com.example.taskmanagementapp.repository;

import com.example.taskmanagementapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User>findByEmail(String email);

    Boolean existsByEmail(String email);

    List<User> findUsersByWorkspacesId(Long id);

    List<User>findUsersByTasksId(Long id);

    Boolean existsByEmailAndWorkspacesId(String email, Long workspaceId);



}
