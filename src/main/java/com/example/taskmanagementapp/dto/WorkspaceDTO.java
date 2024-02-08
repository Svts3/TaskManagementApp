package com.example.taskmanagementapp.dto;

import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.model.User;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WorkspaceDTO {

    private Long id;

    private String name;

    private List<UserDTO> members;

    private UserDTO creator;

}
