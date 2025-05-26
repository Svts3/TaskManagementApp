package com.example.taskmanagementapp.repository;

import com.example.taskmanagementapp.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken>findByToken(String token);

    Boolean existsByUserId(Long id);
    @Modifying
    @Query("delete from RefreshToken r where r.user.id=:id")
    void deleteByUserId(@Param("id")Long id);
}
