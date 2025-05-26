package com.example.taskmanagementapp.auditor.config;

import com.example.taskmanagementapp.auditor.CreatorProvider;
import com.example.taskmanagementapp.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "creatorAuditorAware")
public class PersistenceContextConfig {

    @Bean
    AuditorAware<User> creatorAuditorAware(){
        return new CreatorProvider();
    }

}
