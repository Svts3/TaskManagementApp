package com.example.taskmanagementapp.dto;

import lombok.*;

import java.util.Date;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Date creationDate;
    private Date lastModifiedDate;

}
