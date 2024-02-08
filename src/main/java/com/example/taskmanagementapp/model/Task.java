package com.example.taskmanagementapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tasks")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private String status;

    @Column(name = "creation_date")
    private Date creationDate;
    @Column(name = "last_modified_date")
    private Date lastModifiedDate;
    @Column(name = "deadline_date")
    private Date deadlineDate;

    @ManyToMany()
    @JoinTable(name = "users_tasks", joinColumns = @JoinColumn(name = "task_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private List<User> performers;

    @ManyToOne
    @JoinColumn(name = "creator_id", referencedColumnName = "id")
    private User creator;

    @ManyToOne
    private Workspace workspace;

}
