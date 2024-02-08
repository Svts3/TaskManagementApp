package com.example.taskmanagementapp.dto;

import com.example.taskmanagementapp.model.User;
import com.example.taskmanagementapp.model.Workspace;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TaskDTO {

    private Long id;
    private String title;
    private String content;

    private Date creationDate;
    private Date lastModifiedDate;
    private Date deadlineDate;

    private List<UserDTO> performers;

    private UserDTO creator;

    private WorkspaceDTO workspace;
}
