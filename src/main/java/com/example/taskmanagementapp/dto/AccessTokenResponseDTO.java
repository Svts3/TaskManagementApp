package com.example.taskmanagementapp.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class AccessTokenResponseDTO {

    private String accessToken;

    private String refreshToken;

    // Map of workspace IDs to array of permission strings
    private Map<Long, String[]> permissions;
}
