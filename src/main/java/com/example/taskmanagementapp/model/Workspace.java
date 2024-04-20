package com.example.taskmanagementapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "workspaces")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;

    @CreatedDate
    @Column(name = "creation_date")
    private Date creationDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @ManyToMany
    @JoinTable(name = "users_workspaces", joinColumns = @JoinColumn(name = "workspace_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private List<User> members;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL)
    private List<Task> tasks;

    @ManyToOne
    @CreatedBy
    @JoinColumn(name = "creator_id", referencedColumnName = "id")
    private User creator;
}

