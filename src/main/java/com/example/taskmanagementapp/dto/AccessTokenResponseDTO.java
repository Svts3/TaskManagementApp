package com.example.taskmanagementapp.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AccessTokenResponseDTO {

    private String accessToken;

    private String refreshToken;

}
