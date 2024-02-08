package com.example.taskmanagementapp.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginRequestDTO {

    private String email;
    private String password;
}
