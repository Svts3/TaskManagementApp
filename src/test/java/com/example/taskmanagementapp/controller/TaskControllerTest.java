package com.example.taskmanagementapp.controller;

import com.example.taskmanagementapp.security.config.SecurityConfig;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Import({SecurityConfig.class})
@Transactional
@Testcontainers
public class TaskControllerTest {

    MockMvc mockMvc;

    @Autowired
    FilterChainProxy filterChainProxy;

    @Autowired
    TaskController taskController;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>(
            "mysql"
    );



}
