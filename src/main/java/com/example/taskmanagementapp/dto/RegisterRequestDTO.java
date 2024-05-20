package com.example.taskmanagementapp.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.NonNull;


@Builder
@Getter
public class RegisterRequestDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String password;

}
