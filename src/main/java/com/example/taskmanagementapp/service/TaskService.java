package com.example.taskmanagementapp.service;

import com.example.taskmanagementapp.model.Task;
import com.example.taskmanagementapp.model.User;

import java.util.List;

public interface TaskService extends GeneralService<Task, Long>{

    List<Task> findByWorkspaceId(Long id);

    Task addPerformersToTask(Long taskId, List<Long>userIds);

    Task removePerformerFromTask(Long taskId, Long performerId);
}
