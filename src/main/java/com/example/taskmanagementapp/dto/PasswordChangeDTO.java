package com.example.taskmanagementapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeDTO {
    private String currentPassword;
    private String newPassword;
}
